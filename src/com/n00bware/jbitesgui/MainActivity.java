
package com.n00bware.jbitesgui;
import com.n00bware.jbitesgui.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener {

    private static final String APPEND_CMD = "echo \"%s=%s\" >> /system/build.prop";
    private static final String KILL_PROP_CMD = "busybox sed -i \"/%s/D\" /system/build.prop";
    private static final String REPLACE_CMD = "busybox sed -i \"/%s/ c %<s=%s\" /system/build.prop";
    private static final String SHOWBUILD_PATH = "/system/tmp/showbuild";
    private static final String DISABLE = "disable";
    private static final String MOUNT_PREF = "mount_pref";
    private static final String SYSCTL_PREF = "sysctl_pref";
    private static final String CRON_PREF = "cron_pref";
    private static final String INIT_PREF = "init_pref";
    private static final String OC_PREF = "oc_pref";

    // Strings for installScripts
    public static final String SCRIPTS_PATH = "/system/bin";

    //TAG is used for debugging in logcat
    private static final String TAG = "JBitesGUI";

    private ListPreference mMountPref;
    private ListPreference mSysctlPref;
    private ListPreference mCronPref;
    private ListPreference mInitPref;
    private ListPreference mOCPref;
    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.main_title_head);
        addPreferencesFromResource(R.xml.main);

        Log.d(TAG, "Loading prefs");
        PreferenceScreen prefSet = getPreferenceScreen();

        mMountPref = (ListPreference) prefSet.findPreference(MOUNT_PREF);
        mMountPref.setOnPreferenceChangeListener(this);

        mSysctlPref = (ListPreference) prefSet.findPreference(SYSCTL_PREF);
        mSysctlPref.setOnPreferenceChangeListener(this);

        mCronPref = (ListPreference) prefSet.findPreference(CRON_PREF);
        mCronPref.setOnPreferenceChangeListener(this);

        mInitPref = (ListPreference) prefSet.findPreference(INIT_PREF);
        mInitPref.setOnPreferenceChangeListener(this);

        mOCPref = (ListPreference) prefSet.findPreference(OC_PREF);
        mOCPref.setOnPreferenceChangeListener(this);

        /*
         * Mount /system RW and determine if /system/tmp exists; if it doesn't
         * we make it
         */
        File tmpDir = new File("/system/tmp");
        boolean exists = tmpDir.exists();
        if (!exists) {
            try {
                Log.d(TAG, "We need to make /system/tmp dir");
                Bin.mount("rw");
                Bin.runRootCommand("mkdir /system/tmp");
            } finally {
                Bin.mount("ro");
            }
        }

        /*Install scripts
        File scripts = new File(SCRIPTS_PATH);
        boolean checkScripts = checkScripts.exists();
        if (!checkScritps) {
            try {
                Log.d(TAG, String.format("script not found @ '%s'", SCRIPTS_PATH));
                Bin.remountRW();
                Bin.installScripts();
            } finally {
                Bin.remountRO();
            }
        } */


        // Declare open
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setTitle(R.string.open_title);
        mAlertDialog.setMessage(getResources().getString(R.string.open_summary));
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getResources().getString(com.android.internal.R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        mAlertDialog.show();
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (newValue != null) {
            Log.e(TAG, "New preference selected: " + newValue);
            if (pref == mMountPref) {
                return Bin.mount(newValue.toString());
            } else if (pref == mSysctlPref) {
                Bin.modScripts("sysctl.conf", newValue.toString());
                return Bin.modScripts("init.d/99sysctl", newValue.toString());
            } else if (pref == mCronPref) {
                //Bin.runRootCommand("echo \"root:x:0:0::data/cron:/system/xbin/bash\" > /system/etc/passwd");
                return Bin.modScripts("99cron", newValue.toString());
            } else if (pref == mInitPref) {
                return Bin.modScripts("99init", newValue.toString());
            } else if (pref == mOCPref) {
                return Bin.modScripts("99oc", newValue.toString());
            }
        }
        return false;
    }
}
