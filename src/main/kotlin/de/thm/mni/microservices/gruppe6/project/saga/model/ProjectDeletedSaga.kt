package de.thm.mni.microservices.gruppe6.project.saga.model

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Member
import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project

data class ProjectDeletedSaga(
    private val project: Project,
    ) {

    lateinit var members: List<Member>

    var issuesDeleted = false
    var projectDeleted = true
    var membersDeleted = true

    fun getProjectData(): Project {
        return project
    }

    fun issuesDeleted() {
        issuesDeleted = true
    }

    fun isComplete(): Boolean {
        return issuesDeleted && projectDeleted && membersDeleted
    }

}