package com.eighteengray.tiny_compress;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.tinify.AccountException;
import com.tinify.Source;
import com.tinify.Tinify;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;


public class TinyCompressAction extends AnAction {
    private String groupId = "123";
    private AnActionEvent anActionEvent;
    private JTextField jTextField;
    private ProgressDialog mProgressDialog;

    String hint = "Enter default tiny api key. Or the default key will be used";

    @Override
    public void actionPerformed(AnActionEvent e) {
        anActionEvent = e;

        Project project = e.getProject();
        mProgressDialog = new ProgressDialog(project, false);
        InputApiKeyDialog startDialog = new InputApiKeyDialog("start compress");
        startDialog.show();
    }

    public class InputApiKeyDialog extends DialogWrapper {
        String msg;

        public InputApiKeyDialog(String msg) {
            super(true);
            this.msg = msg;
            init();
            getCancelAction().setEnabled(true);
            setTitle("TinyCompress");
        }

        @Override
        protected JComponent createCenterPanel() {
            JPanel dialogPanel = new JPanel();
            jTextField = new JTextField(hint);
            dialogPanel.add(jTextField);
            return dialogPanel;
        }

        @Override
        protected void createDefaultActions() {
            super.createDefaultActions();
            myOKAction = new OkAction("OK");
        }

        protected class OkAction extends DialogWrapperAction {
            protected OkAction(String name) {
                super(name);
                this.putValue("DefaultAction", Boolean.TRUE);
            }

            protected void doAction(ActionEvent e) {
                Notifications.Bus.notify(new Notification(groupId, "TinyCompress", "start compress", NotificationType.INFORMATION, null));
                String key;
                if(jTextField.getText().equals(hint)){
                    key = "LHZoJXCysEceDReZIsQPWPxdODBxhavW";
                }else {
                    key = jTextField.getText();
                }

                doOKAction();
                Observable.timer(3, TimeUnit.MILLISECONDS)
                        .subscribe(aLong -> {
                            Tinify.setKey(key);
                            ArrayList<String> imagePaths = new ArrayList<>();
                            for (VirtualFile file : getSelectFiles(anActionEvent)) {
                                imagePaths.addAll(getFileArrayList(file));
                            }

                            ApplicationManager.getApplication().invokeLater(() -> showProgressDialog(imagePaths.size()));

                            boolean result = true;
                            int i = 0;
                            for (String path : imagePaths) {
                                Source source = null;
                                try {
                                    source = Tinify.fromFile(path);
                                    source.toFile(path);

                                    i++;
                                    mProgressDialog.setCurrentIndex(i);
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    ApplicationManager.getApplication().invokeLater(() -> mProgressDialog.close(DialogWrapper.OK_EXIT_CODE));
                                    if(e1 instanceof AccountException){
                                        result = false;
                                        Notifications.Bus.notify(new Notification(groupId, "TinyCompress", e1.getMessage(), NotificationType.WARNING, null));
                                        break;
                                    }else {
                                        Notifications.Bus.notify(new Notification(groupId, "TinyCompress", e1.getMessage(), NotificationType.WARNING, null), anActionEvent.getProject());
                                    }

                                }
                            }
                            if(result){
                                Notifications.Bus.notify(new Notification(groupId, "TinyCompress", "Compress complete", NotificationType.INFORMATION, null));
                                Observable.timer(2, TimeUnit.MILLISECONDS).subscribe(new Consumer<Long>() {
                                    @Override
                                    public void accept(Long aLong) throws Exception {
                                        ApplicationManager.getApplication().invokeLater(() -> mProgressDialog.close(DialogWrapper.OK_EXIT_CODE));
                                    }
                                });
                            }

                        });
            }
        }
    }

    private VirtualFile[] getSelectFiles(AnActionEvent e) {
        return CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.getDataContext());
    }

    private ArrayList<String> getFileArrayList(VirtualFile file) {
        ArrayList<String> pathList = new ArrayList<>();
        if (!file.isDirectory()) {
            if (file.getPath().endsWith(".jpg") || file.getPath().endsWith(".jpeg") || file.getPath().endsWith(".png") || file.getPath().endsWith(".webp")) {
                pathList.add(file.getPath());
            }
        } else {
            for (VirtualFile file1 : file.getChildren()) {
                pathList.addAll(getFileArrayList(file1));
            }
        }
        return pathList;
    }

    private void showProgressDialog(int length) {
        mProgressDialog.setTitle("TinyCompress");
        mProgressDialog.setMaxImages(length);
        mProgressDialog.setModal(false);
        mProgressDialog.show();
    }

}