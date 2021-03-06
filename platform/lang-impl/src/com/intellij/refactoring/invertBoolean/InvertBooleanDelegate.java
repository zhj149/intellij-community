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
package com.intellij.refactoring.invertBoolean;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class InvertBooleanDelegate {
  public static final ExtensionPointName<InvertBooleanDelegate> EP_NAME = ExtensionPointName.create("com.intellij.refactoring.invertBoolean");

  @Nullable
  public static InvertBooleanDelegate findInvertBooleanDelegate(PsiElement element) {
    for (InvertBooleanDelegate delegate : Extensions.getExtensions(EP_NAME)) {
      if (delegate.isVisibleOnElement(element)) {
        return delegate;
      }
    }
    return null;
  }

  /**
   * Quick check if element is potentially acceptable by delegate
   * 
   * @return true if element is possible to invert, e.g. variable or method
   */
  public abstract boolean isVisibleOnElement(@NotNull PsiElement element);

  /**
   * @return true if element is of boolean type
   */
  public abstract boolean isAvailableOnElement(@NotNull PsiElement element);

  /**
   * Adjust element to invert, e.g. suggest to refactor super method instead of current
   * 
   * @return null if user canceled the operation
   */
  @Nullable 
  public abstract PsiElement adjustElement(PsiElement element, Project project, Editor editor);

  /**
   * Eventually collect additional elements to rename, e.g. override methods
   * and find expressions which need to be inverted, e.g. return method statements inside the method itself, etc
   * 
   * @param renameProcessor null if element is not named or name was not changed
   */
  public abstract void collectRefElements(PsiElement element,
                                          @Nullable RenameProcessor renameProcessor,
                                          @NotNull String newName,
                                          Collection<PsiElement> elementsToInvert);

  /**
   * Invoked from {@link #getForeignElementToInvert(PsiElement, PsiElement, Language)};
   * should be used to reject usages for elements from foreign language to be refactored
   * @return null, if reference should not be reverted
   */
  public abstract PsiElement getElementToInvert(PsiElement namedElement, PsiElement expression);

  /**
   * Should be called from {@link #collectRefElements(PsiElement, RenameProcessor, String, Collection)}
   * to process found usages in foreign languages
   */
  protected static PsiElement getForeignElementToInvert(PsiElement namedElement,
                                                        PsiElement expression,
                                                        Language language) {
    if (!expression.getLanguage().is(language)){
      final InvertBooleanDelegate delegate = findInvertBooleanDelegate(expression);
      if (delegate != null) {
        return delegate.getElementToInvert(namedElement, expression);
      }
    }
    return null;
  }

  /**
   * Replace expression with created negation
   * @param expression to be inverted, found in {@link #collectRefElements(PsiElement, RenameProcessor, String, Collection)}
   */
  public abstract void replaceWithNegatedExpression(PsiElement expression);

  /**
   * Initialize variable with negated default initializer when default was initially omitted,
   * or invert variable initializer
   */
  public abstract void invertElementInitializer(PsiElement var);
  
  /**
   * Detect usages which can't be inverted
   */
  public void findConflicts(UsageInfo[] usageInfos, MultiMap<PsiElement, String> conflicts) {}
}
