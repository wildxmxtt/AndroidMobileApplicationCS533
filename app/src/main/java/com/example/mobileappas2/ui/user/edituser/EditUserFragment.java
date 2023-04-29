package com.example.mobileappas2.ui.user.edituser;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.mobileappas2.Database.DBDefs;
import com.example.mobileappas2.Database.DBManager;
import com.example.mobileappas2.Database.DataHolders.Orders;
import com.example.mobileappas2.Database.DataHolders.Users;
import com.example.mobileappas2.R;
import com.example.mobileappas2.databinding.FragmentEdituserBinding;
import com.example.mobileappas2.databinding.FragmentUserBinding;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class EditUserFragment extends Fragment {

    private FragmentEdituserBinding binding;
    public int userID;
    public Users user;
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEdituserBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button updateUserButton = binding.updateUserInfoButton;
        updateUserButton.setOnClickListener(view -> updateUserInfo(view));

        Button cancelUpdateButton = binding.cancelEditInfoButton;
        cancelUpdateButton.setOnClickListener(view -> cancelUpdate(view));

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        userID = sharedPref.getInt("userID", 0);


        // GET ALL OF THE TEXT THE USER HAS ENTERED
        TextView username = binding.usernameEditEditText;
        TextView email = binding.emailEditEditText;
        TextView password = binding.passwordEditEditText;
        TextView postcode = binding.postcodeEditEditText;
        TextView address = binding.addressEditEditText;
        TextView phoneNumber = binding.phoneNumberEditEditText;
        TextView hobby = binding.hobbyEditEditText;
        TextView oldPassword = binding.oldpasswordEditText;

        // GET THE CURRENT DATA HELD IN THE DATABASE FOR THIS USER
        DBManager dbManager = new DBManager(getContext());
        dbManager.open();
        user = new Users();
        Cursor cursor = dbManager.fetch(DBDefs.User.TABLE_NAME,
                new String[]{DBDefs.User.C_FULL_NAME, DBDefs.User.C_PASSWORD,
                        DBDefs.User.C_PHONE_NUMBER, DBDefs.User.C_ADDRESS,
                        DBDefs.User.C_EMAIL_ADDRESS, DBDefs.User.C_POSTCODE,
                        DBDefs.User.C_HOBBIES},
                DBDefs.User.C_USER_ID + " like ?",
                new String[]{Integer.toString(userID)},
                null, null, null, null);
        do {
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_FULL_NAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_PASSWORD)));
            user.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_PHONE_NUMBER)));
            user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_ADDRESS)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_EMAIL_ADDRESS)));
            user.setPostcode(cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_POSTCODE)));
            user.setHobby(cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_HOBBIES)));
            user.setID(userID);
        }while(cursor.moveToNext());

        username.setText(user.getFullName());
        password.setText(user.getPassword());
        phoneNumber.setText(user.getPhoneNumber());
        address.setText(user.getAddress());
        email.setText(user.getEmail());
        postcode.setText(user.getPostcode());
        hobby.setText(user.getHobby());

        dbManager.close();

        return root;
    }

    public void updateUserInfo(View view)
    {
        // GET THE CURRENT USER ID & OPEN THE DATABASE
        DBManager dbManager = new DBManager(getContext());
        dbManager.open();


        // GET ALL OF THE TEXT THE USER HAS ENTERED
        String username = binding.usernameEditEditText.getText().toString();
        String email = binding.emailEditEditText.getText().toString();
        String password = binding.passwordEditEditText.getText().toString();
        String postcode = binding.postcodeEditEditText.getText().toString();
        String address = binding.addressEditEditText.getText().toString();
        String phoneNumber = binding.phoneNumberEditEditText.getText().toString();
        String hobby = binding.hobbyEditEditText.getText().toString();
        String oldPassword = binding.oldpasswordEditText.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || postcode.isEmpty() ||
                address.isEmpty() || phoneNumber.isEmpty() || hobby.isEmpty())
        {
            Toast.makeText(
                    getContext(),
                    "Make Sure No Entries Are Empty",
                    Toast.LENGTH_SHORT).show();
            dbManager.close();
            return;
        }

        if (oldPassword.isEmpty())
        {
            Toast.makeText(
                    getContext(),
                    "Please Enter Old Password",
                    Toast.LENGTH_SHORT).show();
            dbManager.close();
            return;
        }

        if (!email.matches(EMAIL_PATTERN))
        {
            Toast.makeText(
                    getContext(),
                    "Please Enter a Valid Email",
                    Toast.LENGTH_SHORT).show();
            dbManager.close();
            return;
        }
        if (!postcode.matches("^([A-Z][A-HJ-Y]?\\d[A-Z\\d]? ?\\d[A-Z]{2}|GIR ?0A{2})$"))
        {
            Toast.makeText(
                    getContext(),
                    "Please Enter a Valid Postcode",
                    Toast.LENGTH_SHORT).show();
            dbManager.close();
            return;
        }
        if (!phoneNumber.matches("^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$"))
        {
            Toast.makeText(
                    getContext(),
                    "Please Enter a Valid Phone Number",
                    Toast.LENGTH_SHORT).show();
            dbManager.close();
            return;
        }

        String date = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter dateFormatter
                    = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            LocalDate d = LocalDate.now(ZoneId.systemDefault());
            date = d.format(dateFormatter);
        }

        if (!username.equals(user.getFullName()))
        {
            // check if the username is unique
            Cursor cursor = dbManager.fetch(DBDefs.User.TABLE_NAME,
                    new String[]{DBDefs.User.C_FULL_NAME},
                    DBDefs.User.C_FULL_NAME + " like ?",
                    new String[]{username},
                    null, null, null, null);
            if (cursor.getCount() > 0)
            {
                Toast.makeText(
                        getContext(),
                        "Username Already Taken",
                        Toast.LENGTH_SHORT).show();
                dbManager.close();
                return;
            }
        }

        if (!email.equals(user.getEmail()))
        {
            // check if the email is unique
            Cursor cursor2 = dbManager.fetch(DBDefs.User.TABLE_NAME,
                    new String[]{DBDefs.User.C_EMAIL_ADDRESS},
                    DBDefs.User.C_EMAIL_ADDRESS + " like ?",
                    new String[]{email},
                    null, null, null, null);
            if (cursor2.getCount() > 0)
            {
                Toast.makeText(
                        getContext(),
                        "Email Already Taken",
                        Toast.LENGTH_SHORT).show();
                dbManager.close();
                return;
            }
        }

        // UPDATE THE CONTENTS OF THE DATABASE
        dbManager.update(username, email, password,
                postcode, address, date, userID,
                phoneNumber, hobby, null);
        dbManager.close();

        // return to the user screen
        Navigation.findNavController(view).navigate(R.id.navigation_user);
    }

    public void cancelUpdate(View view)
    {
        // return to the user screen
        Navigation.findNavController(view).navigate(R.id.navigation_user);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}