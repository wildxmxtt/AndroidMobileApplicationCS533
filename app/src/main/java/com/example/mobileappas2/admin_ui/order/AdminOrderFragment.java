package com.example.mobileappas2.admin_ui.order;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileappas2.Database.DBDefs;
import com.example.mobileappas2.Database.DBManager;
import com.example.mobileappas2.Database.DataHolders.Orders;
import com.example.mobileappas2.Database.DataHolders.Products;
import com.example.mobileappas2.Database.DataHolders.UserOrders;
import com.example.mobileappas2.R;
import com.example.mobileappas2.databinding.AdminFragmentOrdersBinding;
import com.example.mobileappas2.ui.user.OldOrderData;
import com.example.mobileappas2.ui.user.OldOrdersAdapter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;

public class AdminOrderFragment extends Fragment {

    private AdminFragmentOrdersBinding binding;
    private AdminOrderAdapter adapter;
    public ArrayList<String> playerNames = new ArrayList();
    public int userID = 0;

    public ToggleButton[] toggles;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = AdminFragmentOrdersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // INITIALISE THE DATABASE & DATA HOLDER
        DBManager dbManager = new DBManager(getContext());
        dbManager.open();

        // SET UP THE SPINNER TO SHOW THE CATEGORIES AVAILABLE
        // open the database
        // load the data from the database
        Cursor cursor = dbManager.fetch(DBDefs.User.TABLE_NAME,
                new String[]{DBDefs.User.C_FULL_NAME},
                null,
                null,
                null, null, null, null);
        dbManager.close();

        // convert data in database to a usable list of strings
        if (cursor.getCount() > 0) {
            do {
                String name = new String();
                name = cursor.getString(cursor.getColumnIndexOrThrow(DBDefs.User.C_FULL_NAME));
                playerNames.add(name);
            } while (cursor.moveToNext());
        }
        playerNames.add(0, "All");

        // setup the spinner
        Spinner spinner = (Spinner) binding.adminUserSpinner;
        ArrayAdapter arrayAdapter = new ArrayAdapter(
                getActivity(),
                R.layout.spinner_item,
                playerNames);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(arrayAdapter);
        // add listener to add to the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // change the title and the description of the section based on the item selected
                userID = spinner.getSelectedItemPosition();
                updateAdapter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        toggles = new ToggleButton[]{binding.adminOrderToggle1, binding.adminOrderToggle2,
                binding.adminOrderToggle3,binding.adminOrderToggle4};
        toggles[0].setOnClickListener(view -> toggleSelected(0));
        toggles[1].setOnClickListener(view -> toggleSelected(1));
        toggles[2].setOnClickListener(view -> toggleSelected(2));
        toggles[3].setOnClickListener(view -> toggleSelected(3));

        updateAdapter();
        return root;
    }

    public void toggleSelected(int toggleID)
    {
        enableToggles(toggleID);
        /*
        *   0 = making order
        *   1 = dispatched
        *   2 = out for delivery
        *   3 = delivered
        */
        // if there is no item selected then return
        Log.i("DEBUG", "CURRENT ID: " + AdminOrderAdapter.currentSelectedOrderID);
        if (AdminOrderAdapter.currentSelectedOrderID < 0)
            return;
        String date = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter dateFormatter
                    = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            LocalDate d = LocalDate.now(ZoneId.systemDefault());
            date = d.format(dateFormatter);
        }

        DBManager dbManager = new DBManager(getContext());
        dbManager.open();
        dbManager.update(AdminOrderAdapter.currentSelectedOrderID,
                date,
                toggleID,
                null);
        dbManager.close();
        updateAdapter();
    }

    /*
        disables all of the toggles and then will enable the toggle that was pressed
     */
    public void enableToggles(int togglePressed)
    {
        toggles[0].setChecked(false);
        toggles[1].setChecked(false);
        toggles[2].setChecked(false);
        toggles[3].setChecked(false);

        toggles[togglePressed].setChecked(true);
    }

    public void updateAdapter() {
        DBManager dbManager = new DBManager(getContext());
        dbManager.open();
        ArrayList<OrderData> orderData = new ArrayList();

        if (userID == 0) {
            binding.adminUserNameText.setText("All");
        } else {
            // GET THE PLAYER NAME AND THE PLAYER EMAIL TO SHOW ON THE SCREEN
            Cursor cur = dbManager.fetch(DBDefs.User.TABLE_NAME,
                    new String[]{DBDefs.User.C_FULL_NAME},
                    DBDefs.User.C_USER_ID + " like ?",
                    new String[]{Integer.toString(userID)},
                    null, null, null, null);
            String name;
            if (cur.getCount() > 0) {
                do {
                    name = cur.getString(cur.getColumnIndexOrThrow(DBDefs.User.C_FULL_NAME));
                } while (cur.moveToNext());
                binding.adminUserNameText.setText(name);
            }
        }

        Cursor cursor;
        if (userID == 0) {
            // GET ALL OF THE ORDER ID'S
            cursor = dbManager.fetch(DBDefs.User_Order.TABLE_NAME,
                    new String[]{DBDefs.User_Order.C_ORDER_ID, DBDefs.User_Order.C_USER_ID},
                    null, null, null,
                    null, null, null);
        } else {
            // GET ALL OF THE ORDER ID'S FOR THE CURRENT PLAYER
            cursor = dbManager.fetch(DBDefs.User_Order.TABLE_NAME,
                    new String[]{DBDefs.User_Order.C_ORDER_ID, DBDefs.User_Order.C_USER_ID},
                    DBDefs.User_Order.C_USER_ID + " like ?",
                    new String[]{Integer.toString(userID)},
                    null, null, null, null);
        }

        // if there are any orders
        if (cursor.getCount() > 0) {
            // create a list that will hold the orders information
            ArrayList<UserOrders> userOrders = new ArrayList();
            do {
                UserOrders userOrder = new UserOrders();
                userOrder.setOrderID(cursor.getInt(cursor.getColumnIndexOrThrow(DBDefs.User_Order.C_ORDER_ID)));
                userOrder.setUserID(cursor.getInt(cursor.getColumnIndexOrThrow(DBDefs.User_Order.C_USER_ID)));
                userOrders.add(userOrder);
            } while (cursor.moveToNext());

            // for the amount of orders
            for (int i = 0; i < userOrders.size(); i++) {
                // and a instance of orderData to the list of orders (to be filled later)
                OrderData oldData = new OrderData();
                orderData.add(oldData);
            }

            // GET ALL OF THE PRODUCTS FOR EACH OF THE ORDERS I JUST SELECTED & GET THE PRICE
            for (int i = 0; i < userOrders.size(); i++) {
                ArrayList<Products> productList = new ArrayList();
                // product id list
                Cursor cursor2 = dbManager.fetch(DBDefs.Product_Order.TABLE_NAME,
                        new String[]{DBDefs.Product_Order.C_PRODUCT_ID, DBDefs.Product_Order.C_ORDER_ID},
                        DBDefs.Product_Order.C_ORDER_ID + " like ?",
                        new String[]{userOrders.get(i).getOrderID().toString()},
                        null, null, null, null);
                do {
                    // product info list
                    Cursor cursor3 = dbManager.fetch(DBDefs.Product.TABLE_NAME,
                            new String[]{DBDefs.Product.C_PRODUCT_ID, DBDefs.Product.C_PRODUCT_NAME,
                                    DBDefs.Product.C_PRICE},
                            DBDefs.Product.C_PRODUCT_ID + " like ?",
                            new String[]{cursor2.getString(cursor2.getColumnIndexOrThrow(DBDefs.Product_Order.C_PRODUCT_ID))},
                            null, null, null, null);
                    if (cursor3.getCount() > 0) {
                        do {
                            Products products = new Products();
                            products.setName(cursor3.getString(cursor3.getColumnIndexOrThrow(DBDefs.Product.C_PRODUCT_NAME)));
                            products.setPrice(cursor3.getFloat(cursor3.getColumnIndexOrThrow(DBDefs.Product.C_PRICE)));
                            products.setID(cursor3.getInt(cursor3.getColumnIndexOrThrow(DBDefs.Product.C_PRODUCT_ID)));
                            productList.add(products);
                        } while (cursor3.moveToNext());
                    }
                } while (cursor2.moveToNext());
                // take the generated product list and convert it to a string of the correct type
                // and also turn this list into the total cost
                String orderProducts = convertProductListToString(productList);
                String orderTotal = convertProductListToTotalString(productList);
                orderData.get(i).setOrderProducts(orderProducts);
                orderData.get(i).setOrderTotalPrice(orderTotal);
            }

            // GET THE DATE CREATED AND DATE UPDATED FROM THE ORDER TABLE
            for (int i = 0; i < userOrders.size(); i++) {
                ArrayList<Orders> orderList = new ArrayList();
                // product id list
                Cursor cursor1 = dbManager.fetch(DBDefs.Order.TABLE_NAME,
                        new String[]{DBDefs.Order.C_DATE_CREATED, DBDefs.Order.C_DATE_UPDATED, DBDefs.Order.C_STATUS},
                        DBDefs.Product_Order.C_ORDER_ID + " like ?",
                        new String[]{userOrders.get(i).getOrderID().toString()},
                        null, null, null, null);
                do {
                    Orders order = new Orders();
                    order.setDateCreated(cursor1.getString(cursor1.getColumnIndexOrThrow(DBDefs.Order.C_DATE_CREATED)));
                    order.setDateUpdated(cursor1.getString(cursor1.getColumnIndexOrThrow(DBDefs.Order.C_DATE_UPDATED)));
                    order.setStatus(cursor1.getInt(cursor1.getColumnIndexOrThrow(DBDefs.Order.C_STATUS)));
                    order.setID(userOrders.get(i).getOrderID());
                    orderList.add(order);
                } while (cursor1.moveToNext());
                // convert the data to show the current date and the order date in string form
                String orderDate = "Order Made: " + orderList.get(0).getDateCreated();
                String orderUpdated = "Order Updated: " + orderList.get(0).getDateUpdated();
                String orderStatus = "Status";
                if (orderList.get(0).getStatus() == 0)
                    orderStatus = "Status: " + getString(R.string.admin_status1);
                else if (orderList.get(0).getStatus() == 1)
                    orderStatus = "Status: " + getString(R.string.admin_status2);
                else if (orderList.get(0).getStatus() == 2)
                    orderStatus = "Status: " + getString(R.string.admin_status3);
                else if (orderList.get(0).getStatus() == 3)
                    orderStatus = "Status: " + getString(R.string.admin_status4);
                orderData.get(i).setOrderDate(orderDate);
                orderData.get(i).setOrderUpdateDate(orderUpdated);
                orderData.get(i).setOrderStatus(orderStatus);
                orderData.get(i).setOrderID(orderList.get(0).getID());
            }
        }

        // initialise the adapter to hold all of the orders found above.
        adapter = new AdminOrderAdapter(getActivity(), getContext(), orderData , toggles);
        RecyclerView recyclerView = (RecyclerView) binding.adminOrdersRecyclerView;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        dbManager.close();
    }

    public String convertProductListToTotalString(ArrayList<Products> list) {
        float subTotal = 0;
        for (int i = 0; i < list.size(); i++) {
            subTotal += list.get(i).getPrice();
        }
        return "Total: £ " + Float.toString(subTotal);
    }

    public String convertProductListToString(ArrayList<Products> list) {
        String tempString = "";
        for (int i = 0; i < list.size(); i++) {
            tempString += list.get(i).getPrice().toString() + " - " +
                    list.get(i).getName() + "\n";
        }
        return tempString;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}