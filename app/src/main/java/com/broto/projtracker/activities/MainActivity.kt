package com.broto.projtracker.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.broto.projtracker.R
import com.broto.projtracker.adapters.BoardItemsAdapter
import com.broto.projtracker.firebase.FireStoreClass
import com.broto.projtracker.models.Board
import com.broto.projtracker.models.User
import com.broto.projtracker.utils.Constants
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_content.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    FireStoreClass.GetDataFromFirebaseCallbacks,
    FireStoreClass.SignedInUserDetails {

    private val TAG = "MainActivity"
    private lateinit var mUsername: String
    private lateinit var mBoardListAdapter: BoardItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpActionBar()
        main_nav_view.setNavigationItemSelectedListener(this)

        fab_create_board.setOnClickListener {
            if (!this::mUsername.isInitialized) {
                Log.e(TAG, "User details not available. Abort")
                showErrorSnackBar(resources.getString(R.string.cannot_create_board))
                return@setOnClickListener
            }
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.EXTRA_USERNAME, mUsername)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        FireStoreClass.getInstance().getAllBoardsList(this)
        FireStoreClass.getInstance().getCurrentUserData(this)
    }

    private fun populateBoardListRecyclerView(boardList: ArrayList<Board>) {
        Log.d(TAG, "Board List Size: ${boardList.size}")
        hideProgressDialog()

        if (boardList.isEmpty()) {
            rv_main_boards_list.visibility = View.GONE
            tv_main_no_boards.visibility = View.VISIBLE
        } else {
            rv_main_boards_list.visibility = View.VISIBLE
            tv_main_no_boards.visibility = View.GONE

            mBoardListAdapter = BoardItemsAdapter(this, boardList)

            rv_main_boards_list.adapter = mBoardListAdapter

            rv_main_boards_list.layoutManager = LinearLayoutManager(this)
            rv_main_boards_list.setHasFixedSize(true)
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar_main_activity)

        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (main_drawer_layout.isDrawerOpen(GravityCompat.START)) {
            main_drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            main_drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (main_drawer_layout.isDrawerOpen(GravityCompat.START)) {
            main_drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_menu_my_profile -> {
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_menu_signout -> {
                Log.d(TAG, "Sign Out User: ${FirebaseAuth.getInstance().currentUser?.uid}")
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, IntroActivity::class.java)
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                )
                startActivity(intent)
                finish()
            }
        }
        if (main_drawer_layout.isDrawerOpen(GravityCompat.START)) {
            main_drawer_layout.closeDrawer(GravityCompat.START)
        }
        return true
    }

    override fun onFetchDetailsSuccess(user: User?) {
        if (user == null) {
            Log.e(TAG, "User Details not found for " +
                    "${FirebaseAuth.getInstance().currentUser?.uid}")
            return
        }

        mUsername = user.name

        Glide.with(this)
            .load(user.imageData)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_header_user_image)

        nav_header_tv_username.text = user.name
    }

    override fun onFetchDetailsFailed() {
        Log.e(TAG, "Unable fetch user details for UUID: " +
                "${FirebaseAuth.getInstance().currentUser?.uid}")
    }

    override fun onDataAvailable(boardList: ArrayList<Board>) {
        Log.d(TAG, "Board List Retrieved")
        hideProgressDialog()
        populateBoardListRecyclerView(boardList)
    }

    override fun onDataFetchFailed() {
        Log.d(TAG, "Cannot fetch board details")
        hideProgressDialog()
        showErrorSnackBar("Unable to fetch board details")
    }
}