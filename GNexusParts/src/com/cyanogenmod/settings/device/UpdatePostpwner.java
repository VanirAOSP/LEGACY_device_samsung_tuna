/* Nuclearmistake - 2013
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

public class UpdatePostpwner {

    private String mFilePath;

    private String sDefaultValue;
    private String[] aDefaultValues = new String[3];

    private String[] aValues = new String[3];

    private final static String[] PATH_PREFIXES = new String[] {
        "red_", "green_", "blue_"
    };

    String[] fakepaths = new String[3];

    public UpdatePostpwner(String path)
    {
        String prefix="", suffix="";
        mFilePath = path;
        String[] thisShouldBeCleaner = mFilePath.split("/");
        if (thisShouldBeCleaner.length > 0)
        {
            for(int i=0;i<thisShouldBeCleaner.length-1;i++)
            {
                prefix += "/"+thisShouldBeCleaner[i];
            }
            prefix += "/";
            suffix = thisShouldBeCleaner[thisShouldBeCleaner.length-1];
        }
        for (Colors c : Colors.values())
            fakepaths[c.ordinal()] = prefix+PATH_PREFIXES[c.ordinal()]+suffix;
    }

    public int InitializeMultiplier(Colors c, int iMax)
    {
        // Read original value
        if (sDefaultValue == null && Utils.fileExists(mFilePath)) {
            sDefaultValue = Utils.readOneLine(mFilePath);
            String[] vals = sDefaultValue.split(" ");
            for(int i=0;i<vals.length;i++)
            {
                int val = (int)((long)Long.valueOf(vals[i])/2);
                aDefaultValues[i] = aValues[i] = ""+val;
            }
        } else if (aDefaultValues[c.ordinal()] == null) {
            aDefaultValues[c.ordinal()] = aValues[c.ordinal()] = ""+iMax;
        }
        return Integer.valueOf(aDefaultValues[c.ordinal()]);
    }

    public int Initialize(Colors c, int iOffset, int iMax)
    {
        // Read original value
        if (sDefaultValue == null && Utils.fileExists(mFilePath)) {
            sDefaultValue = Utils.readOneLine(mFilePath);
            String[] vals = sDefaultValue.split(" ");
            for(int i=0;i<vals.length;i++)
            {
                aDefaultValues[i] = aValues[i] = vals[i];
            }
        } else if (aDefaultValues[c.ordinal()] == null) {
            aDefaultValues[c.ordinal()] = aValues[c.ordinal()] = ""+(iMax - iOffset);
        }
        return Integer.valueOf(aDefaultValues[c.ordinal()]);
    }

    public String getDefault(Colors c) {
        return aDefaultValues[c.ordinal()];
    }

    public void writeValue(Colors c, String val)
    {
        aValues[c.ordinal()] = val;
        Utils.writeValue(mFilePath, ""+this);
    }

    public void writeColor(Colors c, int value) {
        writeValue(c, ""+((long) value * 2));
    }

    @Override
    public String toString()
    {
        return aValues[Colors.RED.ordinal()]+" "+aValues[Colors.GREEN.ordinal()]+" "+aValues[Colors.BLUE.ordinal()];
    }
}
