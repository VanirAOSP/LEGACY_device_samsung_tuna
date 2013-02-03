/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.settings.device;

public enum Colors {
    RED,
    GREEN,
    BLUE
};

class I2Color {
    static Colors[] Lookup;

    //this could probably be hardcoded, but I want to make sure this doesn't get twisted up.
    static void Init() {
        if (Lookup != null) return;
        Lookup = new Colors[Colors.values().length];
        for (Colors c : Colors.values())
            Lookup[c.ordinal()] = c;
    }
}
