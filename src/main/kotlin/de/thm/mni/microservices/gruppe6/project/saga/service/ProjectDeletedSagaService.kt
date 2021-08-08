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

/**
 * Service responsible for initiating a project deletion saga,
 * subsequently listing for associated events and initiating rollbacks or completing the saga.
 * @param projectRepository the project repository.
 * @param memberRepository the member repository.
 * @param jmsTemplate the JMSTemplate to send events.
 * @see ProjectDeletedSaga
 */
@Service
class ProjectDeletedSagaService(
    private val projectRepository: ProjectRepository,
    private val memberRepository: MemberRepository,
    private val jmsTemplate: JmsTemplate
    ) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    // Hash Map associating ProjectDeletedSagas with project ids.
    private val projectDeletedSagas: HashMap<UUID, ProjectDeletedSaga> = HashMap()

    /**
     * Starts a project deletion saga for the given projects id.
     * This function:
     *  1. Finds the project within the database.
     *  2. Creates the ProjectDeletedSaga within the map.
     *  3. Reads all members of the project from the database.
     *  4. Inserts the members into the ProjectDeletedSaga.
     *  5. Deletes the project and members (by cascade) from the database.
     *  6. Sends a ProjectSagaEvent with status BEGIN via ActiveMQ.
     * @param projectId the project id to be deleted.
     */
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

    /**
     * Takes a received ProjectSagaEvent and executes relevant functions.
     * @param sagaEvent The ProjectSagaEvent.
     */
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

    /**
     * Function called to record that the issue-service has reported issue deletion.
     * If issue deletion failed: rollback saga transactions.
     * If the saga is completed ->
     */
    private fun issuesDeleted(sagaEvent: ProjectSagaEvent) {
        val saga = projectDeletedSagas[sagaEvent.referenceValue]!!
        if (!sagaEvent.success) {
            this.rollbackSagaTransaction(saga)
            return
        }
        saga.issuesDeleted()
        if (saga.isComplete()) {
            sagaComplete(saga)
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
        projectDeletedSagas.remove(project.id!!)
    }

    fun sagaComplete(saga: ProjectDeletedSaga) {
        logger.debug("Completed delete saga for project {}/{}", saga.getProjectData().name, saga.getProjectData().id)
        projectDeletedSagas.remove(saga.getProjectData().id!!)
        jmsTemplate.convertAndSend(
            EventTopic.SagaEvents.topic,
            ProjectSagaEvent(
                referenceValue = saga.getProjectData().id!!,
                projectSagaStatus = ProjectSagaStatus.COMPLETE,
                success = false)
        )
    }

}