package de.thm.mni.microservices.gruppe6.project.saga.model

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Member
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project

/**
 * Class holding all relevant information, including rollback data and status of a saga transaction.
 * @param project the Project to be deleted.
 * @see de.thm.mni.microservices.gruppe6.project.saga.service.ProjectDeletedSagaService
 */
data class ProjectDeletedSaga(
    val project: Project,
    ) {

    // Members deleted / to be restored.
    lateinit var members: List<Member>

    var issuesDeleted = false
    var projectDeleted = true
    var membersDeleted = true

    /**
     * Sets the state of issuesDeleted to true.
     */
    fun issuesDeleted() {
        issuesDeleted = true
    }

    /**
     * Returns true, when project, members and issues have been deleted successfully.
     */
    fun isComplete(): Boolean {
        return issuesDeleted && projectDeleted && membersDeleted
    }

}