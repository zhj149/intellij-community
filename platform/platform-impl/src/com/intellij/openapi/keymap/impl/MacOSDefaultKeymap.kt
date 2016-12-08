/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.intellij.openapi.keymap.impl

import com.intellij.configurationStore.SchemeDataHolder
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.actionSystem.MouseShortcut
import com.intellij.openapi.actionSystem.Shortcut
import com.intellij.util.containers.mapSmart
import org.intellij.lang.annotations.JdkConstants
import java.awt.event.InputEvent
import javax.swing.KeyStroke

class MacOSDefaultKeymap(dataHolder: SchemeDataHolder<KeymapImpl>) : DefaultKeymapImpl(dataHolder) {
  override fun getParentActionIds(firstKeyStroke: KeyStroke) = super.getParentActionIds(convertKeyStroke(firstKeyStroke))

  override fun getParentActionIds(shortcut: MouseShortcut) = super.getParentActionIds(convertMouseShortcut(shortcut))

  override fun getParentShortcuts(actionId: String) = super.getParentShortcuts(actionId).mapSmart { convertShortcutFromParent(it) }

  companion object {
    @JvmStatic
    fun convertShortcutFromParent(parentShortcut: Shortcut): Shortcut {
      if (parentShortcut is MouseShortcut) {
        return convertMouseShortcut(parentShortcut)
      }

      val key = parentShortcut as KeyboardShortcut
      return KeyboardShortcut(convertKeyStroke(key.firstKeyStroke), key.secondKeyStroke?.let(::convertKeyStroke))
    }
  }
}

private fun convertKeyStroke(parentKeyStroke: KeyStroke): KeyStroke = KeyStroke.getKeyStroke(parentKeyStroke.keyCode, mapModifiers(parentKeyStroke.modifiers), parentKeyStroke.isOnKeyRelease)

private fun convertMouseShortcut(macShortcut: MouseShortcut) = MouseShortcut(macShortcut.button, mapModifiers(macShortcut.modifiers), macShortcut.clickCount)

@JdkConstants.InputEventMask
private fun mapModifiers(@JdkConstants.InputEventMask modifiers: Int): Int {
  var modifiers = modifiers
  var meta = false

  if (modifiers and InputEvent.META_MASK != 0) {
    modifiers = modifiers and InputEvent.META_MASK.inv()
    meta = true
  }

  var metaDown = false
  if (modifiers and InputEvent.META_DOWN_MASK != 0) {
    modifiers = modifiers and InputEvent.META_DOWN_MASK.inv()
    metaDown = true
  }

  var control = false
  if (modifiers and InputEvent.CTRL_MASK != 0) {
    modifiers = modifiers and InputEvent.CTRL_MASK.inv()
    control = true
  }

  var controlDown = false
  if (modifiers and InputEvent.CTRL_DOWN_MASK != 0) {
    modifiers = modifiers and InputEvent.CTRL_DOWN_MASK.inv()
    controlDown = true
  }

  if (meta) {
    modifiers = modifiers or InputEvent.CTRL_MASK
  }

  if (metaDown) {
    modifiers = modifiers or InputEvent.CTRL_DOWN_MASK
  }

  if (control) {
    modifiers = modifiers or InputEvent.META_MASK
  }

  if (controlDown) {
    modifiers = modifiers or InputEvent.META_DOWN_MASK
  }

  return modifiers
}
