package de.thm.mni.microservices.gruppe6.project.saga.service

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project
import de.thm.mni.microservices.gruppe6.lib.event.DeletedIssuesSagaEvent
import de.thm.mni.microservices.gruppe6.lib.event.SagaReferenceType
import de.thm.mni.microservices.gruppe6.project.model.persistence.ProjectRepository
import de.thm.mni.microservices.gruppe6.project.saga.model.ProjectDeletedSaga
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap

@Service
class ProjectDeletedSagaService(private val projectRepository: ProjectRepository) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val projectDeletedSagas: HashMap<UUID, ProjectDeletedSaga> = HashMap()

    fun startSaga(project: Project) {
        logger.debug("Saga for project {} / {} deletion begins!", project.id, project.name)
        projectDeletedSagas.computeIfAbsent(project.id!!) { ProjectDeletedSaga(project) }
    }

    fun receiveSagaEvent(sagaEvent: DeletedIssuesSagaEvent) {
        if (sagaEvent.referenceType != SagaReferenceType.PROJECT || !projectDeletedSagas.containsKey(sagaEvent.referenceValue)) {
            logger.error("Received event does not reference any continued saga!")
            return
        }
        val saga = projectDeletedSagas[sagaEvent.referenceValue]!!
        if (!sagaEvent.success) {
            val project = saga.restoreProject()
            logger.debug("Restored project {}", sagaEvent.referenceValue)
            this.projectRepository.save(project)
            projectDeletedSagas.remove(sagaEvent.referenceValue)
            return
        }
        saga.issuesDeleted()
        if (saga.isComplete()) {
            logger.debug("Completed delete saga for project {}", sagaEvent.referenceValue)
            projectDeletedSagas.remove(sagaEvent.referenceValue)
        }
    }

}