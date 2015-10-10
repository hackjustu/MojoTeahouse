package com.mojoteahouse.mojotea.fragment;

import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mojoteahouse.mojotea.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartCustomerDetailFragment extends Fragment implements View.OnClickListener {

    private static final String PHONE_NUMBER_REGEX = "^(\\+0?1\\s)?\\(?\\d{3}\\)?\\s*[.-]?\\s*\\d{3}\\s*[.-]?\\s*\\d{4}\\s*$";
    // Min deliver time is 15 mins
    private static final int MIN_DELIVER_TIME_MINS = 15;

    private TextInputLayout nameTextLayout;
    private TextInputLayout addressTextLayout;
    private TextInputLayout phoneTextLayout;
    private EditText nameEditText;
    private EditText addressEditText;
    private EditText phoneEditText;
    private EditText noteEditText;
    private ImageButton clearNameButton;
    private ImageButton clearAddressButton;
    private ImageButton clearPhoneButton;
    private ImageButton clearNoteButton;
    private TextView currentDateText;
    private Button timeButton;
    private Calendar selectedDeliverTime;
    private SimpleDateFormat simpleTimeFormat;

    public static CartCustomerDetailFragment newInstance() {
        CartCustomerDetailFragment fragment = new CartCustomerDetailFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public CartCustomerDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart_customer_detail, container, false);

        nameTextLayout = (TextInputLayout) view.findViewById(R.id.name_edit_text_layout);
        addressTextLayout = (TextInputLayout) view.findViewById(R.id.address_edit_text_layout);
        phoneTextLayout = (TextInputLayout) view.findViewById(R.id.phone_edit_text_layout);
        nameEditText = (EditText) view.findViewById(R.id.name_edit_text);
        addressEditText = (EditText) view.findViewById(R.id.address_edit_text);
        phoneEditText = (EditText) view.findViewById(R.id.phone_edit_text);
        noteEditText = (EditText) view.findViewById(R.id.note_edit_text);
        clearNameButton = (ImageButton) view.findViewById(R.id.name_clear_button);
        clearAddressButton = (ImageButton) view.findViewById(R.id.address_clear_button);
        clearPhoneButton = (ImageButton) view.findViewById(R.id.phone_clear_button);
        clearNoteButton = (ImageButton) view.findViewById(R.id.note_clear_button);
        currentDateText = (TextView) view.findViewById(R.id.current_date_text);
        timeButton = (Button) view.findViewById(R.id.time_button);

        nameTextLayout.setErrorEnabled(true);
        addressTextLayout.setErrorEnabled(true);
        phoneTextLayout.setErrorEnabled(true);

        setupEditTexts();

        clearNameButton.setOnClickListener(this);
        clearAddressButton.setOnClickListener(this);
        clearPhoneButton.setOnClickListener(this);
        clearNoteButton.setOnClickListener(this);
        timeButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        selectedDeliverTime = Calendar.getInstance();
        selectedDeliverTime.add(Calendar.MINUTE, MIN_DELIVER_TIME_MINS);
        selectedDeliverTime.set(Calendar.SECOND, 59);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.US);
        currentDateText.setText(simpleDateFormat.format(selectedDeliverTime.getTime()));
        simpleTimeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        updateTimeText();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.name_clear_button:
                nameEditText.setText("");
                nameTextLayout.setError(null);
                break;

            case R.id.address_clear_button:
                addressEditText.setText("");
                addressTextLayout.setError(null);
                break;

            case R.id.phone_clear_button:
                phoneEditText.setText("");
                phoneTextLayout.setError(null);
                break;

            case R.id.note_clear_button:
                noteEditText.setText("");
                break;

            case R.id.time_button:
                showTimePickerDialog();
                break;
        }
    }

    public boolean checkAllFields() {
        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameTextLayout.setError(getString(R.string.name_error_message));
            return false;
        }
        if (TextUtils.isEmpty(addressEditText.getText()) || addressEditText.getText().length() < 10) {
            addressTextLayout.setError(getString(R.string.address_error_message));
            return false;
        }
        if (TextUtils.isEmpty(phoneEditText.getText()) || !isPhoneNumberValid(phoneEditText.getText().toString())) {
            phoneTextLayout.setError(getString(R.string.phone_error_message));
            return false;
        }
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, MIN_DELIVER_TIME_MINS - 2);
        if (selectedDeliverTime.getTimeInMillis() < now.getTimeInMillis()) {
            Toast.makeText(getActivity(), R.string.deliver_time_error_message, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean isPhoneNumberValid(String number) {
        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    private void showTimePickerDialog() {
        int currentHour = selectedDeliverTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = selectedDeliverTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDeliverTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDeliverTime.set(Calendar.MINUTE, minute);
                        selectedDeliverTime.set(Calendar.SECOND, 59);
                        updateTimeText();
                    }
                }, currentHour, currentMinute, false);

        timePickerDialog.show();
    }

    private void updateTimeText() {
        timeButton.setText(simpleTimeFormat.format(selectedDeliverTime.getTime()));
    }

    private void setupEditTexts() {
        nameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    addressEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                nameTextLayout.setError(null);
                clearNameButton.setVisibility(TextUtils.isEmpty(s)
                        ? View.GONE
                        : View.VISIBLE);
            }
        });

        addressEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    phoneEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });
        addressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                addressTextLayout.setError(null);
                clearAddressButton.setVisibility(TextUtils.isEmpty(s)
                        ? View.GONE
                        : View.VISIBLE);
            }
        });

        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                phoneTextLayout.setError(null);
                clearPhoneButton.setVisibility(TextUtils.isEmpty(s)
                        ? View.GONE
                        : View.VISIBLE);
            }
        });

        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                clearNoteButton.setVisibility(TextUtils.isEmpty(s)
                        ? View.GONE
                        : View.VISIBLE);
            }
        });
    }
}
