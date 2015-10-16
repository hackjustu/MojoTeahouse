package com.mojoteahouse.mojotea.activity;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mojoteahouse.mojotea.MojoTeaApp;
import com.mojoteahouse.mojotea.R;
import com.mojoteahouse.mojotea.data.Order;
import com.mojoteahouse.mojotea.data.OrderItem;
import com.mojoteahouse.mojotea.fragment.ClosedNowDialogFragment;
import com.mojoteahouse.mojotea.fragment.ConfirmOrderDialogFragment;
import com.mojoteahouse.mojotea.fragment.PlacingOrderDialogFragment;
import com.mojoteahouse.mojotea.util.DataUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceOrderActivity extends AppCompatActivity implements View.OnClickListener,
        ConfirmOrderDialogFragment.PlaceOrderListener {

    public static final String NAME_SPLIT_SYMBOL = "   ";
    public static final String TOPPING_SPLIT_SYMBOL = ", ";

    private static final String TAG_CONFIRM_ORDER_DIALOG = "TAG_CONFIRM_ORDER_DIALOG";
    private static final String TAG_PLACING_ORDER_DIALOG = "TAG_PLACING_ORDER_DIALOG";
    private static final String TAG_CLOSED_NOW = "TAG_CLOSED_NOW";
    private static final String PHONE_NUMBER_REGEX = "^(\\+0?1\\s)?\\(?\\d{3}\\)?\\s*[.-]?\\s*\\d{3}\\s*[.-]?\\s*\\d{4}\\s*$";
    // Min deliver time is 15 mins
    private static final int MIN_DELIVER_TIME_MINS = 30;

    private TextInputLayout nameTextLayout;
    private TextInputLayout addressTextLayout;
    private TextInputLayout zipTextLayout;
    private TextInputLayout phoneTextLayout;
    private EditText nameEditText;
    private EditText addressEditText;
    private EditText zipEditText;
    private EditText phoneEditText;
    private EditText noteEditText;
    private ImageButton clearNameButton;
    private ImageButton clearAddressButton;
    private ImageButton clearZipButton;
    private ImageButton clearPhoneButton;
    private ImageButton clearNoteButton;
    private Button dateAndTimeButton;
    private TextView dateAndTimeErrorText;
    private Button placeOrderButton;
    private SharedPreferences sharedPreferences;
    private ConnectivityManager connectivityManager;
    private Calendar selectedDeliverTime;
    private SimpleDateFormat simpleDateFormat;

    private Order order;
    private Date deliverTime;
    private String customerName;
    private String customerAddress;
    private String customerZip;
    private String customerPhone;
    private String customerNote;
    private boolean isClosedNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.place_order_toolbar_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        nameTextLayout = (TextInputLayout) findViewById(R.id.name_edit_text_layout);
        addressTextLayout = (TextInputLayout) findViewById(R.id.address_edit_text_layout);
        zipTextLayout = (TextInputLayout) findViewById(R.id.zip_edit_text_layout);
        phoneTextLayout = (TextInputLayout) findViewById(R.id.phone_edit_text_layout);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        addressEditText = (EditText) findViewById(R.id.address_edit_text);
        zipEditText = (EditText) findViewById(R.id.zip_edit_text);
        phoneEditText = (EditText) findViewById(R.id.phone_edit_text);
        noteEditText = (EditText) findViewById(R.id.note_edit_text);
        clearNameButton = (ImageButton) findViewById(R.id.name_clear_button);
        clearAddressButton = (ImageButton) findViewById(R.id.address_clear_button);
        clearZipButton = (ImageButton) findViewById(R.id.zip_clear_button);
        clearPhoneButton = (ImageButton) findViewById(R.id.phone_clear_button);
        clearNoteButton = (ImageButton) findViewById(R.id.note_clear_button);
        dateAndTimeButton = (Button) findViewById(R.id.date_and_time_button);
        dateAndTimeErrorText = (TextView) findViewById(R.id.date_and_time_error_text);
        placeOrderButton = (Button) findViewById(R.id.bottom_action_button);

        nameTextLayout.setErrorEnabled(true);
        addressTextLayout.setErrorEnabled(true);
        zipTextLayout.setErrorEnabled(true);
        phoneTextLayout.setErrorEnabled(true);

        setupEditTexts();
        noteEditText.clearFocus();

        clearNameButton.setOnClickListener(this);
        clearAddressButton.setOnClickListener(this);
        clearZipButton.setOnClickListener(this);
        clearPhoneButton.setOnClickListener(this);
        clearNoteButton.setOnClickListener(this);
        dateAndTimeButton.setOnClickListener(this);
        placeOrderButton.setOnClickListener(this);

        selectedDeliverTime = Calendar.getInstance();
        selectedDeliverTime.add(Calendar.MINUTE, MIN_DELIVER_TIME_MINS);
        selectedDeliverTime.set(Calendar.SECOND, 59);
        simpleDateFormat = new SimpleDateFormat("EEE, MMM dd yyyy   h:mm a", Locale.US);
        updateDateAndTimeText();

        isClosedNow = sharedPreferences.getBoolean(MojoTeaApp.PREF_CLOSED_NOW, false);
        if (isClosedNow) {
            showClosedNowDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        placeOrderButton.setEnabled(!isClosedNow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        supportFinishAfterTransition();
        return true;
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

            case R.id.zip_clear_button:
                zipEditText.setText("");
                zipTextLayout.setError(null);
                break;

            case R.id.phone_clear_button:
                phoneEditText.setText("");
                phoneTextLayout.setError(null);
                break;

            case R.id.note_clear_button:
                noteEditText.setText("");
                break;

            case R.id.date_and_time_button:
                showDatePickerDialog();
                break;

            case R.id.bottom_action_button:
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isNetworkConnected = (activeNetworkInfo != null) && (activeNetworkInfo.isConnected());
                if (sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0) == 0) {
                    Toast.makeText(this, R.string.place_order_no_item_error_message, Toast.LENGTH_LONG).show();
                } else if (!isNetworkConnected) {
                    Toast.makeText(this, R.string.no_network_place_order_error_message, Toast.LENGTH_LONG).show();
                } else {
                    if (checkAllFields()) {
                        hideKeyboard();
                        FragmentManager fragmentManager = getFragmentManager();
                        ConfirmOrderDialogFragment fragment
                                = (ConfirmOrderDialogFragment) fragmentManager.findFragmentByTag(TAG_CONFIRM_ORDER_DIALOG);
                        if (fragment == null) {
                            fragment = ConfirmOrderDialogFragment.newInstance();
                        }
                        fragment.show(fragmentManager, TAG_CONFIRM_ORDER_DIALOG);
                    }
                }
                break;
        }
    }

    @Override
    public void onPlaceOrderConfirmed() {
        ParseQuery<OrderItem> orderItemQuery = OrderItem.getQuery();
        orderItemQuery.fromLocalDatastore();
        orderItemQuery.whereEqualTo(OrderItem.ORDER_PLACED, false);
        orderItemQuery.findInBackground(new FindCallback<OrderItem>() {
            @Override
            public void done(List<OrderItem> orderItemList, ParseException e) {
                if (e != null) {
                    Toast.makeText(PlaceOrderActivity.this, R.string.place_order_error_message, Toast.LENGTH_LONG).show();
                } else {
                    showPlacingOrderDialog();
                    saveOrder(orderItemList);
                }
            }
        });
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
                    zipEditText.requestFocus();
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

        zipEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    phoneEditText.requestFocus();
                    return true;
                }
                return false;
            }
        });
        zipEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                zipTextLayout.setError(null);
                clearZipButton.setVisibility(TextUtils.isEmpty(s)
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

        nameEditText.setText(sharedPreferences.getString(MojoTeaApp.PREF_CUSTOMER_NAME, ""));
        addressEditText.setText(sharedPreferences.getString(MojoTeaApp.PREF_CUSTOMER_ADDRESS, ""));
        zipEditText.setText(sharedPreferences.getString(MojoTeaApp.PREF_CUSTOMER_ZIP, ""));
        phoneEditText.setText(sharedPreferences.getString(MojoTeaApp.PREF_CUSTOMER_PHONE, ""));
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(nameEditText.getWindowToken(), 0);
    }

    private boolean checkAllFields() {
        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameTextLayout.setError(getString(R.string.name_error_message));
            return false;
        }
        Editable address = addressEditText.getText();
        if (TextUtils.isEmpty(address) || address.length() < 10) {
            addressTextLayout.setError(getString(R.string.address_error_message));
            return false;
        }
        Editable zip = zipEditText.getText();
        if (TextUtils.isEmpty(zip) || zip.length() != 5) {
            zipTextLayout.setError(getString(R.string.zip_error_message));
            return false;
        }
        Editable phone = phoneEditText.getText();
        if (TextUtils.isEmpty(phone) || !isPhoneNumberValid(phone.toString())) {
            phoneTextLayout.setError(getString(R.string.phone_error_message));
            return false;
        }
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, MIN_DELIVER_TIME_MINS - 2);
        if (selectedDeliverTime.getTimeInMillis() < now.getTimeInMillis()) {
            Toast.makeText(this, R.string.deliver_too_soon_error_message, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void showClosedNowDialog() {
        Fragment fragment = getFragmentManager().findFragmentByTag(TAG_CLOSED_NOW);
        if (fragment == null) {
            ClosedNowDialogFragment.newInstance().show(getFragmentManager(), TAG_CLOSED_NOW);
        }
    }

    private void showDatePickerDialog() {
        int year = selectedDeliverTime.get(Calendar.YEAR);
        int month = selectedDeliverTime.get(Calendar.MONTH);
        int day = selectedDeliverTime.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedDeliverTime.set(Calendar.YEAR, year);
                        selectedDeliverTime.set(Calendar.MONTH, monthOfYear);
                        selectedDeliverTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        showTimePickerDialog();
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        int currentHour = selectedDeliverTime.get(Calendar.HOUR_OF_DAY);
        int currentMinute = selectedDeliverTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDeliverTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDeliverTime.set(Calendar.MINUTE, minute);
                        selectedDeliverTime.set(Calendar.SECOND, 59);
                        updateDateAndTimeText();
                        verifyDateAndTime();
                    }
                }, currentHour, currentMinute, false);

        timePickerDialog.show();
    }

    private void updateDateAndTimeText() {
        dateAndTimeButton.setText(simpleDateFormat.format(selectedDeliverTime.getTime()));
    }

    private void verifyDateAndTime() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, MIN_DELIVER_TIME_MINS);
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, 2);
        future.set(Calendar.HOUR_OF_DAY, 21);
        future.set(Calendar.MINUTE, 0);
        future.set(Calendar.SECOND, 0);
        int hour = selectedDeliverTime.get(Calendar.HOUR_OF_DAY);
        if (selectedDeliverTime.before(now)) {
            dateAndTimeErrorText.setText(R.string.deliver_too_soon_error_message);
            dateAndTimeErrorText.setVisibility(View.VISIBLE);
            placeOrderButton.setEnabled(false);
        } else if (selectedDeliverTime.after(future)) {
            dateAndTimeErrorText.setText(R.string.deliver_date_error_message);
            dateAndTimeErrorText.setVisibility(View.VISIBLE);
            placeOrderButton.setEnabled(false);
        } else if (hour < 12 || hour > 21) {
            dateAndTimeErrorText.setText(R.string.deliver_business_time_error_message);
            dateAndTimeErrorText.setVisibility(View.VISIBLE);
            placeOrderButton.setEnabled(false);
        } else {
            dateAndTimeErrorText.setVisibility(View.GONE);
            if (!isClosedNow) {
                placeOrderButton.setEnabled(true);
            }
        }
    }

    private void showPlacingOrderDialog() {
        PlacingOrderDialogFragment fragment
                = (PlacingOrderDialogFragment) getFragmentManager().findFragmentByTag(TAG_PLACING_ORDER_DIALOG);
        if (fragment == null) {
            fragment = PlacingOrderDialogFragment.newInstance();
        }
        fragment.show(getFragmentManager(), TAG_PLACING_ORDER_DIALOG);
    }

    private void dismissPlacingOrderDialog() {
        PlacingOrderDialogFragment fragment
                = (PlacingOrderDialogFragment) getFragmentManager().findFragmentByTag(TAG_PLACING_ORDER_DIALOG);
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    private void saveOrder(List<OrderItem> orderItemList) {
        saveCustomerDetails();
        if (order == null) {
            order = new Order();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM dd yyyy, h:mm a", Locale.US);
            order.setOrderTime(simpleDateFormat.format(new Date()));
            order.setDeliverBy(simpleDateFormat.format(deliverTime));
            order.setCustomerName(customerName);
            order.setCustomerAddress(String.format(getString(R.string.customer_address_format), customerAddress, customerZip));
            order.setCustomerPhone(customerPhone);
            order.setCustomerNote(customerNote);
            order.setTotalQuantity(sharedPreferences.getInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0));
            order.setOrderItemList(orderItemList);
            double totalPrice = 0;
            List<String> completeOrderList = new ArrayList<>();
            List<String> toppingList;
            StringBuilder stringBuilder;
            OrderItem orderItem;
            for (int i = 0; i < orderItemList.size(); i++) {
                orderItem = orderItemList.get(i);
                orderItem.setOrderPlaced(true);
                totalPrice += orderItem.getTotalPrice();
                stringBuilder = new StringBuilder();
                stringBuilder.append(orderItem.getName())
                        .append(NAME_SPLIT_SYMBOL);
                toppingList = orderItem.getSelectedToppingsList();
                for (int j = 0; j < toppingList.size(); j++) {
                    if (j > 0) {
                        stringBuilder.append(TOPPING_SPLIT_SYMBOL);
                    }
                    stringBuilder.append(toppingList.get(j));
                }
                completeOrderList.add(stringBuilder.toString());
            }
            order.setTotalPrice(totalPrice);
            order.setCompleteOrderList(completeOrderList);
            order.setAnonymousUserId(DataUtils.getDeviceId(PlaceOrderActivity.this));

            ParseObject.pinAllInBackground(MojoTeaApp.ORDER_ITEM_GROUP, orderItemList);
        }

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isNetworkConnected = (activeNetworkInfo != null) && (activeNetworkInfo.isConnected());
        if (isNetworkConnected) {
            saveOrderToRemoteAndFinish();
        } else {
            dismissPlacingOrderDialog();
            Toast.makeText(this, R.string.no_network_place_order_error_message, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isPhoneNumberValid(String number) {
        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);
        Matcher matcher = pattern.matcher(number);
        return matcher.matches();
    }

    private void saveCustomerDetails() {
        deliverTime = selectedDeliverTime.getTime();
        customerName = (nameEditText.getText() == null ? "" : nameEditText.getText().toString());
        customerAddress = (addressEditText.getText() == null ? "" : addressEditText.getText().toString());
        customerZip = (zipEditText.getText() == null ? "" : zipEditText.getText().toString());
        customerPhone = (phoneEditText.getText() == null ? "" : phoneEditText.getText().toString());
        customerNote = (noteEditText.getText() == null ? "" : noteEditText.getText().toString());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MojoTeaApp.PREF_CUSTOMER_NAME, customerName);
        editor.putString(MojoTeaApp.PREF_CUSTOMER_ADDRESS, customerAddress);
        editor.putString(MojoTeaApp.PREF_CUSTOMER_ZIP, customerZip);
        editor.putString(MojoTeaApp.PREF_CUSTOMER_PHONE, customerPhone);
        editor.apply();
    }

    private void saveOrderToRemoteAndFinish() {
        order.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    dismissPlacingOrderDialog();
                    Toast.makeText(PlaceOrderActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    order.pinInBackground(MojoTeaApp.ORDER_GROUP, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            dismissPlacingOrderDialog();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_COUNT, 0);
                            editor.putFloat(MojoTeaApp.PREF_LOCAL_ORDER_TOTAL_PRICE, 0);
                            editor.putStringSet(MojoTeaApp.PREF_LOCAL_ORDER_ITEM_CONTENT_SET, new HashSet<String>());
                            editor.apply();

                            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("h:mm a", Locale.US);
                            Intent intent = new Intent(PlaceOrderActivity.this, PostOrderActivity.class);
                            intent.putExtra(PostOrderActivity.EXTRA_TOTAL_PRICE, order.getTotalPrice());
                            intent.putExtra(PostOrderActivity.EXTRA_DELIVER_TIME,
                                    simpleTimeFormat.format(deliverTime));
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });
    }
}
