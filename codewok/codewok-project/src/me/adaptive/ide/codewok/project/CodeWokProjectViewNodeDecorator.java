/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.adaptive.ide.codewok.project;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import me.adaptive.ide.branding.CodeWokIcons;
import me.adaptive.ide.icons.AdaptiveIcons;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by panthro on 5/05/15.
 */
public class CodeWokProjectViewNodeDecorator implements ProjectViewNodeDecorator {

    private static class DecorateInfo {
        private Icon icon;
        private String tooltip;

        public DecorateInfo(Icon icon, String tooltip) {
            this.icon = icon;
            this.tooltip = tooltip;
        }

        public Icon getIcon() {
            return icon;
        }

        public String getTooltip() {
            return tooltip;
        }
    }

    private static Map<String, DecorateInfo> DECORATE_INFO_MAP = new HashMap<String, DecorateInfo>();

    static {
        DECORATE_INFO_MAP.put("node_modules", new DecorateInfo(AdaptiveIcons.NodeJs, "NPM modules"));
        DECORATE_INFO_MAP.put("package.json", new DecorateInfo(AdaptiveIcons.NodeJs, "Npm config file"));
        DECORATE_INFO_MAP.put("bower_components", new DecorateInfo(AdaptiveIcons.BowerIcon, "Bower components"));
        DECORATE_INFO_MAP.put("bower.json", new DecorateInfo(AdaptiveIcons.BowerIcon, "Bower cofig file"));
        DECORATE_INFO_MAP.put("config", new DecorateInfo(AllIcons.General.ProjectSettings, "Your project config files"));
        DECORATE_INFO_MAP.put("Gruntfile.js", new DecorateInfo(AdaptiveIcons.Grunt, "Grunt config file"));
        DECORATE_INFO_MAP.put("src", new DecorateInfo(AllIcons.Json.Object, "Your project source files"));
        DECORATE_INFO_MAP.put("index.html", new DecorateInfo(null, "Your project Main entry point"));

    }


    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        DecorateInfo info = DECORATE_INFO_MAP.get(data.getPresentableText());
        if (info != null) {
            if (info.getIcon() != null) {
                data.setIcon(DECORATE_INFO_MAP.get(data.getPresentableText()).getIcon());
            }
            if (info.getTooltip() != null) {
                data.setTooltip(DECORATE_INFO_MAP.get(data.getPresentableText()).getTooltip());
            }
        } else if (node != null && node.getProject().getBaseDir().getName().equals(data.getPresentableText())) {
            data.setIcon(CodeWokIcons.CodeWok_16);
        }


    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {

    }
}
