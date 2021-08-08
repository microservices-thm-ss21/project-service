package de.thm.mni.microservices.gruppe6.project.saga.service

import de.thm.mni.microservices.gruppe6.lib.event.*
import de.thm.mni.microservices.gruppe6.project.model.persistence.MemberRepository
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.saga.model.ProjectDeletedSaga
import org.slf4j.LoggerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import reactor.core.scheduler.Schedulers
import java.util.*
import kotlin.collections.HashMap

@Service
class ProjectDeletedSagaService(
    private val projectRepository: ProjectRepository,
    private val memberRepository: MemberRepository,
    private val jmsTemplate: JmsTemplate
    ) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val projectDeletedSagas: HashMap<UUID, ProjectDeletedSaga> = HashMap()

    fun startSaga(projectId: UUID) {
        projectRepository
            .findById(projectId)
            .doOnNext { project ->
                logger.debug("Saga for project {} / {} deletion begins!", project.id, project.name)
                projectDeletedSagas.computeIfAbsent(project.id!!) {
                    ProjectDeletedSaga(project)
                }
            }.flatMapMany {
                memberRepository.getMembersByProjectID(it.id!!).collectList()
            }.map {
                projectDeletedSagas[projectId]!!.apply { members = it }
            }.doOnNext {
                projectRepository.deleteById(projectId).subscribe()
            }.publishOn(Schedulers.boundedElastic())
            .doOnNext {
                jmsTemplate.convertAndSend(
                    EventTopic.SagaEvents.topic,
                    ProjectSagaEvent(projectId, ProjectSagaStatus.BEGIN, true)
                )
            }
            .subscribe()
    }

    fun receiveSagaEvent(sagaEvent: ProjectSagaEvent) {
        if (sagaEvent.referenceType != SagaReferenceType.PROJECT || !projectDeletedSagas.containsKey(sagaEvent.referenceValue)) {
            logger.error("Received event does not reference any continued saga!")
            return
        }
        when (sagaEvent.projectSagaStatus) {
            ProjectSagaStatus.ISSUES_DELETED -> issuesDeleted(sagaEvent)
            else -> {} // ignore
        }
    }

    private fun issuesDeleted(sagaEvent: ProjectSagaEvent) {
        val saga = projectDeletedSagas[sagaEvent.referenceValue]!!
        if (!sagaEvent.success) {
            this.rollbackSagaTransaction(saga)
            return
        }
        saga.issuesDeleted()
        if (saga.isComplete()) {
            logger.debug("Completed delete saga for project {}", sagaEvent.referenceValue)
            projectDeletedSagas.remove(sagaEvent.referenceValue)
        }
    }

    fun rollbackSagaTransaction(saga: ProjectDeletedSaga) {
        val project = saga.getProjectData()
        val members = saga.members
        logger.debug("Restore project {}/{}", project.name, project.id)
        this.projectRepository
            .save(project)
            .doOnNext {
                memberRepository.saveAll(members).subscribe()
            }
            .publishOn(Schedulers.boundedElastic())
            .doOnNext {
                jmsTemplate.convertAndSend(
                    EventTopic.SagaEvents.topic,
                    ProjectSagaEvent(
                        referenceValue = project.id!!,
                        projectSagaStatus = ProjectSagaStatus.COMPLETE,
                        success = false)
                )
            }.subscribe()
        projectDeletedSagas.remove(project.id)
    }

}