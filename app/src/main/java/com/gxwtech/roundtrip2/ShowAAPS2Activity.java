package com.gxwtech.roundtrip2;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.pump.common.hw.rileylink.service.RileyLinkService;
import info.nightscout.androidaps.plugins.pump.omnipod.OmnipodManager;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.PodInfoType;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalSchedule;
import info.nightscout.androidaps.plugins.pump.omnipod.defs.schedule.BasalScheduleEntry;
import info.nightscout.androidaps.plugins.pump.omnipod.service.RileyLinkOmnipodService;
import info.nightscout.androidaps.plugins.pump.omnipod.util.OmniPodConst;
import info.nightscout.androidaps.utils.SP;

public class ShowAAPS2Activity extends AppCompatActivity {

    private static final Logger LOG = LoggerFactory.getLogger(ShowAAPS2Activity.class);

    Spinner spinner;

    Button btnStart, btnResetPodStatus;

    Map<String, CommandAction> allCommands = new HashMap<>();
    private BroadcastReceiver mBroadcastReceiver;
    private TextView tvDuration, tvAmount, tvCommandStatusText, textViewComm, tvPodStatus, tvPodStatusText;
    private EditText tfDuration, tfAmount;
    CommandAction selectedCommandAction = null;

    public ShowAAPS2Activity() {
    }

    private void addCommandAction(String action, ImplementationStatus implementationStatus, String intent) {
        allCommands.put(action, new CommandAction(action, implementationStatus, intent));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addCommandAction(getResources().getString(R.string.cmd_aaps_initialize_pod), ImplementationStatus.Done, "RefreshData.InitializePod");
        addCommandAction(getResources().getString(R.string.cmd_aaps_insert_cannula), ImplementationStatus.Done, "RefreshData.InsertCannula");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_status), ImplementationStatus.Done, "RefreshData.GetStatus");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_time), ImplementationStatus.Done, "RefreshData.GetTime");
        addCommandAction(getResources().getString(R.string.cmd_aaps_acknowledge_alerts), ImplementationStatus.Done, "RefreshData.AcknowledgeAlerts");
        addCommandAction(getResources().getString(R.string.cmd_aaps_set_basal_profile), ImplementationStatus.Done, "RefreshData.SetBasalProfile");
        addCommandAction(getResources().getString(R.string.cmd_aaps_set_tbr), ImplementationStatus.Done, "RefreshData.SetTBR");
        addCommandAction(getResources().getString(R.string.cmd_aaps_cancel_tbr), ImplementationStatus.Done, "RefreshData.CancelTBR");
        addCommandAction(getResources().getString(R.string.cmd_aaps_set_bolus), ImplementationStatus.Done, "RefreshData.Bolus");
        addCommandAction(getResources().getString(R.string.cmd_aaps_cancel_bolus), ImplementationStatus.Done, "RefreshData.CancelBolus");
        addCommandAction(getResources().getString(R.string.cmd_aaps_suspend_delivery), ImplementationStatus.Done, "RefreshData.SuspendDelivery");
        addCommandAction(getResources().getString(R.string.cmd_aaps_resume_delivery), ImplementationStatus.Done, "RefreshData.ResumeDelivery");
        addCommandAction(getResources().getString(R.string.cmd_aaps_set_time), ImplementationStatus.Done, "RefreshData.SetTime");

        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_active_alerts), ImplementationStatus.Done, "RefreshData.PodInfoActiveAlerts");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_data_log), ImplementationStatus.Done, "RefreshData.PodInfoDataLog");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_fault_and_initialization_time), ImplementationStatus.Done, "RefreshData.PodInfoFaultAndInitializationTime");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_fault_event), ImplementationStatus.Done, "RefreshData.PodInfoFaultEvent");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_low_flash_log_dump), ImplementationStatus.Done, "RefreshData.PodInfoLowFlashLogDump");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_recent_high_flash_log_dump), ImplementationStatus.Done, "RefreshData.PodInfoRecentHighFlashLogDump");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_older_high_flash_log_dump), ImplementationStatus.Done, "RefreshData.PodInfoOlderHighFlashLogDump");
        addCommandAction(getResources().getString(R.string.cmd_aaps_get_pod_info_test_values), ImplementationStatus.Done, "RefreshData.PodInfoTestValues");

        addCommandAction(getResources().getString(R.string.cmd_aaps_deactivate_pod), ImplementationStatus.Done, "RefreshData.DeactivatePod");

        setContentView(R.layout.activity_show_aaps2);

        this.textViewComm = findViewById(R.id.textViewComm);

        this.tvDuration = findViewById(R.id.tvDuration);
        this.tvAmount = findViewById(R.id.tvAmount);

        this.tvPodStatus = findViewById(R.id.tvPodStatus);
        this.tvPodStatusText = findViewById(R.id.tvPodStatusText);

        this.tfAmount = findViewById(R.id.tfAmount);
        this.tfDuration = findViewById(R.id.tfDuration);

        this.btnStart = findViewById(R.id.btnStart);
        this.btnStart.setOnClickListener(v -> {
            startAction();
        });

        this.btnResetPodStatus = findViewById(R.id.btnResetPodStatus);
        this.btnResetPodStatus.setOnClickListener(v -> new AlertDialog.Builder(ShowAAPS2Activity.this)
                .setTitle("Reset Pod Status")
                .setMessage("Are you sure you want to reset the pod status? The pod status can not be restored. If the pod is still active, you won't be able to control it anymore.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    SP.remove(OmniPodConst.Prefs.POD_STATE);
                    getOmnipodManager().resetPodState();
                    updatePodState();
                })
                .setNegativeButton(android.R.string.no, null).show());

        tvCommandStatusText = findViewById(R.id.tvCommandStatusText);
        spinner = findViewById(R.id.spinnerPumpCommands);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object itemAtPosition = parent.getItemAtPosition(position);
                commandSelected(itemAtPosition);
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                commandSelected(null);
            }
        });

        updatePodState();

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                /*
                 * here we can listen for local broadcasts, then send ourselves
                 * a specific intent to deal with them, if we wish
                 */
                if (intent == null) {
                    LOG.error("onReceive: received null intent");
                } else {
                    String action = intent.getAction();
                    sendData(action);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();

        for (CommandAction commandAction : allCommands.values()) {

            if (commandAction.implementationStatus == ImplementationStatus.Done || //
                    commandAction.implementationStatus == ImplementationStatus.WorkInProgress) {
                if (commandAction.intentString != null) {
                    intentFilter.addAction(commandAction.intentString);
                }
            }
        }

        intentFilter.addAction("RefreshData.Error");

        LocalBroadcastManager.getInstance(MainApp.instance().getApplicationContext()).registerReceiver(
                mBroadcastReceiver, intentFilter);
    }


    public void commandSelected(Object id) {

        if (id == null) {
            tvCommandStatusText.setText("Nothing");
            enableFields(false, false);
            this.btnStart.setEnabled(false);
        } else {

            this.selectedCommandAction = allCommands.get((String) id);
            tvCommandStatusText.setText(selectedCommandAction.implementationStatus.text);
            enableFields(isAmountEnabled(), isDurationEnabled());
            this.btnStart.setEnabled((selectedCommandAction.implementationStatus == ImplementationStatus.Done || //
                    selectedCommandAction.implementationStatus == ImplementationStatus.WorkInProgress));
        }

    }


    private boolean isAmountEnabled() {
        String action = this.selectedCommandAction.action;

        return (action.equals(getResources().getString(R.string.cmd_aaps_set_tbr)) || //
                action.equals(getResources().getString(R.string.cmd_aaps_set_bolus)) || //
                action.equals(getResources().getString(R.string.cmd_aaps_set_basal_profile)) || //
                action.equals(getResources().getString(R.string.cmd_aaps_set_ext_bolus)) //
        );
    }


    private boolean isDurationEnabled() {
        String action = this.selectedCommandAction.action;

        return (action.equals(getResources().getString(R.string.cmd_aaps_set_tbr)) || action.equals(getResources().getString(R.string.cmd_aaps_set_ext_bolus)));
    }


    private void enableFields(boolean amount, boolean duration) {

        tfDuration.setEnabled(duration);
        tvDuration.setEnabled(duration);
        if (!duration)
            tfDuration.setText("");

        tvAmount.setEnabled(amount);
        tfAmount.setEnabled(amount);

        if (!amount)
            tfAmount.setText("");

    }


    public void putOnDisplay(String text) {
        this.textViewComm.append(text + "\n");
    }

    public enum ImplementationStatus {
        NotStarted("Not Started"), //
        WorkInProgress("Work In Progress"), //
        Done("Command Done"), //
        NotSupportedByDevice("Not supported by device"); //

        String text;


        ImplementationStatus(String text) {
            this.text = text;
        }
    }

    public class CommandAction {

        String action;
        ImplementationStatus implementationStatus;
        String intentString;


        public CommandAction(String action, //
                             ImplementationStatus implementationStatus, //
                             String intentString) {
            this.action = action;
            this.implementationStatus = implementationStatus;
            this.intentString = intentString;
        }

    }

    private OmnipodManager getOmnipodManager() {
        RileyLinkService rileyLinkService = RileyLinkUtil.getRileyLinkService();
        return ((RileyLinkOmnipodService) rileyLinkService).getOmnipodManager();
    }

    Object data;
    String errorMessage;

    public void sendData(String action) {

        switch (action) {
            case "RefreshData.InitializePod":
            case "RefreshData.InsertCannula":
            case "RefreshData.GetStatus":
            case "RefreshData.GetTime":
            case "RefreshData.AcknowledgeAlerts":
            case "RefreshData.SetBasalProfile":
            case "RefreshData.SetTBR":
            case "RefreshData.CancelTBR":
            case "RefreshData.Bolus":
            case "RefreshData.CancelBolus":
            case "RefreshData.SuspendDelivery":
            case "RefreshData.ResumeDelivery":
            case "RefreshData.SetTime":
            case "RefreshData.PodInfoActiveAlerts":
            case "RefreshData.PodInfoDataLog":
            case "RefreshData.PodInfoFaultAndInitializationTime":
            case "RefreshData.PodInfoFaultEvent":
            case "RefreshData.PodInfoLowFlashLogDump":
            case "RefreshData.PodInfoRecentHighFlashLogDump":
            case "RefreshData.PodInfoOlderHighFlashLogDump":
            case "RefreshData.PodInfoTestValues":
            case "RefreshData.DeactivatePod":
                putOnDisplay(data == null ? "null" : data.toString());
                break;
            case "RefreshData.Error":
                putOnDisplay("Error: " + errorMessage);
                break;
            default:
                putOnDisplay("Unsupported action: " + action);
        }

        data = null;
        btnStart.setEnabled((selectedCommandAction.implementationStatus == ImplementationStatus.Done || //
                selectedCommandAction.implementationStatus == ImplementationStatus.WorkInProgress));
    }

    private void updatePodState() {
        runOnUiThread(() -> tvPodStatusText.setText(getOmnipodManager().getPodStateAsString()));
    }

    private void startAction() {

        putOnDisplay("Start Action: " + selectedCommandAction.action);

        this.btnStart.setEnabled(false);

        new Thread(() -> {

            LOG.info("start Action: " + selectedCommandAction.action);

            data = null;
            errorMessage = null;

            switch (selectedCommandAction.intentString) {
                case "RefreshData.InitializePod":
                    try {
                        getOmnipodManager().pairAndPrime();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.InsertCannula":
                    try {
                        getOmnipodManager().insertCannula();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.GetStatus":
                    try {
                        data = getOmnipodManager().getStatus();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.GetTime":
                    data = getOmnipodManager().getTime();
                    break;
                case "RefreshData.AcknowledgeAlerts":
                    try {
                        getOmnipodManager().acknowledgeAlerts();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.SetBasalProfile":
                    try {
                        Double amount = getAmount();
                        if (amount != null) {
                            List<BasalScheduleEntry> basalScheduleEntries = new ArrayList<>();
                            for (int i = 0; i < 24; i++) {
                                basalScheduleEntries.add(new BasalScheduleEntry(i % 2 == 0 ? amount : (amount * 2), Duration.standardHours(i)));
                            }
                            BasalSchedule basalSchedule = new BasalSchedule(basalScheduleEntries);
                            getOmnipodManager().setBasalSchedule(basalSchedule, false);
                        }
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.SetTBR":
                    try {
                        TempBasalPair tempBasalPair = getTempBasalPair();
                        if (tempBasalPair != null) {
                            getOmnipodManager().setTempBasal(tempBasalPair.getRate(), tempBasalPair.getDuration());
                            data = getOmnipodManager().getPodStateAsString();
                        }
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.CancelTBR":
                    try {
                        getOmnipodManager().cancelTempBasal();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.Bolus":
                    try {
                        Double units = getAmount();
                        getOmnipodManager().bolus(units);
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.CancelBolus":
                    try {
                        getOmnipodManager().cancelBolus();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.SuspendDelivery":
                    try {
                        getOmnipodManager().suspendDelivery();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.ResumeDelivery":
                    try {
                        getOmnipodManager().resumeDelivery();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.SetTime":
                    try {
                        getOmnipodManager().setTime();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;
                case "RefreshData.PodInfoActiveAlerts":
                    getPodInfo(PodInfoType.ACTIVE_ALERTS);
                    break;
                case "RefreshData.PodInfoDataLog":
                    getPodInfo(PodInfoType.DATA_LOG);
                    break;
                case "RefreshData.PodInfoFaultAndInitializationTime":
                    getPodInfo(PodInfoType.FAULT_AND_INITIALIZATION_TIME);
                    break;
                case "RefreshData.PodInfoFaultEvent":
                    getPodInfo(PodInfoType.FAULT_EVENT);
                    break;
                case "RefreshData.PodInfoLowFlashLogDump":
                    getPodInfo(PodInfoType.LOW_FLASH_DUMP_LOG);
                    break;
                case "RefreshData.PodInfoRecentHighFlashLogDump":
                    getPodInfo(PodInfoType.RECENT_HIGH_FLASH_LOG_DUMP);
                    break;
                case "RefreshData.PodInfoOlderHighFlashLogDump":
                    getPodInfo(PodInfoType.OLDER_HIGH_FLASH_LOG_DUMP);
                    break;
                case "RefreshData.PodInfoTestValues":
                    getPodInfo(PodInfoType.HARDCODED_TEST_VALUES);
                    break;
                case "RefreshData.DeactivatePod":
                    try {
                        getOmnipodManager().deactivatePod();
                        data = getOmnipodManager().getPodStateAsString();
                    } catch (RuntimeException ex) {
                        errorMessage = ex.getMessage();
                        LOG.error("Caught exception: " + errorMessage);
                        ex.printStackTrace();
                    }
                    break;

                default:
                    LOG.warn("Action is not supported {}.", selectedCommandAction);

            }

            if (data == null) {
                RileyLinkUtil.sendBroadcastMessage("RefreshData.Error");
            } else {
                RileyLinkUtil.sendBroadcastMessage(selectedCommandAction.intentString);
            }

            updatePodState();
        }).start();

    }

    private void getPodInfo(PodInfoType infoType) {
        try {
            data = getOmnipodManager().getPodInfo(infoType);
        } catch (RuntimeException ex) {
            errorMessage = ex.getMessage();
            LOG.error("Caught exception: " + errorMessage);
            ex.printStackTrace();
        }
    }

    private TempBasalPair getTempBasalPair() {
        Double rate = getAmount();
        Integer durationInMinutes = getDuration();

        if (rate != null && durationInMinutes != null) {
            return new TempBasalPair(rate, Duration.standardMinutes(durationInMinutes));
        }

        return null;
    }


    private double getAmount() {
        CharSequence am = tfAmount.getText();
        String amount = am.toString().replaceAll(",", ".");

        return Double.parseDouble(amount);
    }


    private Integer getDuration() {
        CharSequence am = tfDuration.getText();
        String duration = am.toString();

        int timeMin = 0;

        if (duration.contains(".") || duration.contains(",")) {
            putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
            return null;
        }

        if (duration.contains(":")) {
            String[] time = duration.split(":");

            if ((!time[1].equals("00")) && (!time[1].equals("30"))) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }

            try {
                timeMin += Integer.parseInt(time[0]) * 60;
            } catch (Exception ex) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }

            if (time[1].equals("30")) {
                timeMin += 30;
            }
        } else {
            try {
                timeMin += Integer.parseInt(duration) * 60;
            } catch (Exception ex) {
                putOnDisplay("Invalid duration: duration must be in minutes or as HH:mm (only 30 min intervals are valid).");
                return null;
            }
        }

        return timeMin;
    }

    private static class TempBasalPair {
        private final double rate;
        private final Duration duration;

        public TempBasalPair(double rate, Duration duration) {
            this.rate = rate;
            this.duration = duration;
        }

        public double getRate() {
            return rate;
        }

        public Duration getDuration() {
            return duration;
        }
    }
}
