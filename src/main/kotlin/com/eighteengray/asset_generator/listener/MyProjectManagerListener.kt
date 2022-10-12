package com.eighteengray.asset_generator.listener

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.psi.PsiManager


class MyProjectManagerListener : ProjectManagerListener {
    private val eventsMap = mutableMapOf<Project, PsiTreeListener>()

    override fun projectOpened(project: Project) {
        super.projectOpened(project)
        val treeListener = PsiTreeListener(project)
        eventsMap[project] = treeListener
        PsiManager.getInstance(project).addPsiTreeChangeListener(
                treeListener)
    }

    override fun projectClosing(project: Project) {
        super.projectClosing(project)
        eventsMap.remove(project)?.let {
            PsiManager.getInstance(project).removePsiTreeChangeListener(it)
        }
    }

}