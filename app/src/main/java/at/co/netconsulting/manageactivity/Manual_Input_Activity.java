package at.co.netconsulting.manageactivity;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import at.co.netconsulting.manageactivity.database.DBHelper;
import at.co.netconsulting.manageactivity.general.BaseActivity;
import at.co.netconsulting.manageactivity.general.MinMaxFilter;

public class Manual_Input_Activity extends BaseActivity {

    private EditText editTextHostname;
    private EditText editTextGroupname;
    private EditText editTextIpaddress;
    private EditText editTextBroadcast;
    private EditText editTextNicMac;
    private EditText editTextComment;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextSSH_Port;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);

        initializeObjects();

        editTextHostname = findViewById(R.id.editTextHostname);
        editTextGroupname = findViewById(R.id.editTextGroupname);
        editTextIpaddress = findViewById(R.id.editTextIpaddress);
        editTextBroadcast = findViewById(R.id.editTextBroadcast);
        editTextNicMac = findViewById(R.id.editTextNicMac);
        editTextComment = findViewById(R.id.editTextComment);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextSSH_Port = findViewById(R.id.editTextSSH_Port);
        editTextSSH_Port.setFilters( new InputFilter[]{ new MinMaxFilter( 1, 65535 )}) ;
    }

    private void initializeObjects() {
        dbHelper = new DBHelper(getApplicationContext());
    }

    public void save(View view) {
        dbHelper.insertWifi(editTextHostname.getText().toString(),
                editTextGroupname.getText().toString(),
                editTextIpaddress.getText().toString(),
                editTextBroadcast.getText().toString(),
                editTextNicMac.getText().toString(),
                editTextComment.getText().toString(),
                editTextUsername.getText().toString(),
                editTextPassword.getText().toString(),
                editTextSSH_Port.getText().toString());
    }
}