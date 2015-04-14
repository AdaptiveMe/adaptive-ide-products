/*
 * Copyright 2014-2015. Adaptive.me.
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

package me.adaptive.ide.codewok.project.generator;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.platform.LocationNameFieldsBinding;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.ListCellRendererWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created by panthro on 14/04/15.
 */
public class CodeWokGenerateProjectDialog extends DialogWrapper {


    private JPanel myRootPane;
    private JTextField projectNameTextField;
    private TextFieldWithBrowseButton locaTionTextField;
    private JComboBox adaptiveVersionComboBox;
    private JComboBox boilerPlateTemplateComboBox;
    private JCheckBox typescriptCheckbox;


    public CodeWokGenerateProjectDialog(@Nullable Project project) {
        super(project,true);
        setTitle("Create New CodeWok Project");
        init();
        /**
         * Location Field handling
         */
        new LocationNameFieldsBinding(project,locaTionTextField,projectNameTextField, ProjectUtil.getBaseDir(),"Project Location");

        /**
         * Adaptive version Combo
         */
        DefaultComboBoxModel adaptiveVersionModel = new DefaultComboBoxModel();
        for (GeneratorRunner.AdaptiveVersion version : GeneratorRunner.AdaptiveVersion.values()) {
            adaptiveVersionModel.addElement(version);
        }
        adaptiveVersionComboBox.setModel(adaptiveVersionModel);
        adaptiveVersionComboBox.setRenderer(createNamedEnumListCellRenderer());

        /**
         * Boilerplace Combo Box
         */

        DefaultComboBoxModel boilerplateModel = new DefaultComboBoxModel();
        for (GeneratorRunner.Boilerplate boilerplate : GeneratorRunner.Boilerplate.values()) {
            boilerplateModel.addElement(boilerplate);
        }
        boilerPlateTemplateComboBox.setModel(boilerplateModel);
        boilerPlateTemplateComboBox.setRenderer(createNamedEnumListCellRenderer());

        /**
         * Validation setup
         */
        locaTionTextField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                checkValid();
            }
        });



    }

    protected void checkValid(){
        String projectName = projectNameTextField.getText();

        if (projectName.trim().isEmpty()) {
            setOKActionEnabled(false);
            setErrorText("Project name cannot be empty");
            return;
        }

        if (locaTionTextField.getText().indexOf('$') >= 0) {
            setOKActionEnabled(false);
            setErrorText("Prject directory name must not contain $ character");
            return;
        }
        setOKActionEnabled(true);
        setErrorText(null);
    }

    protected ListCellRenderer createNamedEnumListCellRenderer(){
        return new ListCellRendererWrapper<Enum>() {
            @Override
            public void customize(JList list, Enum value, int index, boolean selected, boolean hasFocus) {
                if(value != null){
                    if(value instanceof GeneratorRunner.AdaptiveVersion) {
                        setText(((GeneratorRunner.AdaptiveVersion) value).getName());
                    }else if(value instanceof GeneratorRunner.Boilerplate){
                        setText(((GeneratorRunner.Boilerplate) value).getName());
                    }
                }
            }
        };
    }


    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myRootPane;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return projectNameTextField;
    }

    public String getProjectName(){
        return projectNameTextField.getText();
    }

    public String getProjectLocation(){
        return locaTionTextField.getText();
    }

    public GeneratorRunner.AdaptiveVersion getAdaptiveVersion(){
        return (GeneratorRunner.AdaptiveVersion) adaptiveVersionComboBox.getModel().getSelectedItem();
    }

    public GeneratorRunner.Boilerplate getBoilerplate(){
        return (GeneratorRunner.Boilerplate) boilerPlateTemplateComboBox.getModel().getSelectedItem();
    }

    public boolean isTypeScriptEnabled(){
        return typescriptCheckbox.isSelected();
    }
}
