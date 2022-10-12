package com.eighteengray.asset_generator.actions

import com.eighteengray.asset_generator.utils.FileGenerator
import com.eighteengray.asset_generator.utils.FileHelper.shouldActivateFor
import com.eighteengray.asset_generator.utils.PluginUtils.showNotify
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys


class AssetsGeneratorAction : AnAction() {
    private var fileGenerator: FileGenerator? = null

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        if (shouldActivateFor(project!!)) {
            fileGenerator = fileGenerator ?: FileGenerator(project)
            fileGenerator!!.generate()
        } else {
            showNotify("This project is not the flutter project")
        }
    }
}