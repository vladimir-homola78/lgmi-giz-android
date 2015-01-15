package com.ibrow.de.giz.siegelklarheit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Error dialog when we cannot access the camera
 *
 * @see com.ibrow.de.giz.siegelklarheit.ScanActivity
 * @deprecated
 */
public class CameraErrorDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
        builder.setMessage(R.string.no_camera_message + " (" + getArguments().getString("error_message") + ")");
        builder.setTitle(R.string.no_camera_title);
        builder.setPositiveButton(R.string.no_camera_retry_btn, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });
        return builder.create();
    }

}
