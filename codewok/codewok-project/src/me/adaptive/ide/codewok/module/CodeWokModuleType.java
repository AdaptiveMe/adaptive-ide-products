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

package me.adaptive.ide.codewok.module;

import com.intellij.ide.util.projectWizard.EmptyModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by panthro on 14/04/15.
 */
public class CodeWokModuleType extends ModuleType<EmptyModuleBuilder> {


  @NonNls public static final String CODEWOK_MODULE = "CODEWOK_MODULE";

  protected CodeWokModuleType(){
    super(CODEWOK_MODULE);
  }

  public static ModuleType getInstance() {
    return ModuleTypeManager.getInstance().findByID(CODEWOK_MODULE);
  }

  @NotNull
  @Override
  public EmptyModuleBuilder createModuleBuilder() {
    return new EmptyModuleBuilder() {
      @Override
      public ModuleType getModuleType() {
        return getInstance();
      }
    };
  }

  @NotNull
  @Override
  public String getName() {
    return "CodeWok Module";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Adaptive CodeWok Modules are used to develop Adaptive applications using the CodeWok IDE";
  }

  @Override
  public Icon getBigIcon() {
    return null; //TODO add icon
  }

  @Override
  public Icon getNodeIcon(@Deprecated boolean isOpened) {
    return null;//TODO add icon
  }
}
