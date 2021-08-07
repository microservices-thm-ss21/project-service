package de.thm.mni.microservices.gruppe6.project.saga.model

import de.thm.mni.microservices.gruppe6.lib.classes.projectService.Project


data class ProjectDeletedSaga(private val project: Project) {

    var issuesDeleted = false
    var projectDeleted = true

    fun restoreProject(): Project {
        projectDeleted = false
        return project
    }

    fun issuesDeleted() {
        issuesDeleted = true
    }

    fun isComplete(): Boolean {
        return issuesDeleted && projectDeleted
    }

}