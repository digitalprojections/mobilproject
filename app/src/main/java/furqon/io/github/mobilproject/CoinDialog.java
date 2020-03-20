package furqon.io.github.mobilproject;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class CoinDialog extends AppCompatDialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Use coins to unlock");
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.multiple_choice_for_use_coin_dialog, null);
        builder.setView(view);
        builder.setItems(R.array.unlock_actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Toast.makeText(getApplicationContext(), i + " selected", Toast.LENGTH_SHORT).show();
                switch (i){
                    case 0:
                        int coins = sharedPref.read(sharedPref.COINS, 0);
                        if(coins>0){
                            MarkAsAwarded(Integer.parseInt(suraNumber));
                        }else{
                            Toast.makeText(getApplicationContext(),  R.string.not_enough_coins, Toast.LENGTH_LONG).show();
                        }
                        //Toast.makeText(getApplicationContext(),  R.string.use_coins, Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        //Toast.makeText(getApplicationContext(),  R.string.earn_coins, Toast.LENGTH_SHORT).show();
                        Intent intent;
                        intent = new Intent(context, EarnCoinsActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        //Toast.makeText(getApplicationContext(),  R.string.cancel, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
// Get the layout inflater

        //SetCoinValues();
// Create the AlertDialog
        final AlertDialog dialog = builder.create();
    }
}
