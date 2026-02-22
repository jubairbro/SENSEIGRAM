package com.senseigram.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.senseigram.R
import com.senseigram.data.Prefs
import com.senseigram.databinding.FragmentHomeBinding
import com.senseigram.ui.adapters.ChannelGridAdapter
import com.senseigram.ui.adapters.HomeDraftAdapter
import com.senseigram.ui.main.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: Prefs
    private lateinit var channelAdapter: ChannelGridAdapter
    private lateinit var draftAdapter: HomeDraftAdapter

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isEditing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        setupAdapters()
        setupWelcomeCard()
        setupButtons()
        setupNetworkMonitor()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupAdapters() {
        channelAdapter = ChannelGridAdapter { chat ->
            // Click channel -> switch to Compose tab with chatId
            val activity = requireActivity() as? MainActivity ?: return@ChannelGridAdapter
            activity.switchToTab(R.id.nav_compose)
        }
        binding.channelsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.channelsRecycler.adapter = channelAdapter

        draftAdapter = HomeDraftAdapter { draft ->
            // Click draft -> switch to Compose tab
            val activity = requireActivity() as? MainActivity ?: return@HomeDraftAdapter
            activity.switchToTab(R.id.nav_compose)
        }
        binding.draftsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.draftsRecycler.adapter = draftAdapter
    }

    private fun setupWelcomeCard() {
        updateWelcomeText()

        binding.editNameBtn.setOnClickListener {
            if (isEditing) {
                // Save name
                val newName = binding.nameEditInput.text.toString().trim()
                if (newName.isNotEmpty()) {
                    prefs.userName = newName
                }
                binding.nameEditInput.visibility = View.GONE
                binding.welcomeText.visibility = View.VISIBLE
                updateWelcomeText()
                hideKeyboard()
                isEditing = false
            } else {
                // Show edit input
                binding.welcomeText.visibility = View.GONE
                binding.nameEditInput.visibility = View.VISIBLE
                binding.nameEditInput.setText(prefs.userName.ifEmpty { getString(R.string.sensei) })
                binding.nameEditInput.requestFocus()
                binding.nameEditInput.setSelection(binding.nameEditInput.text.length)
                showKeyboard(binding.nameEditInput)
                isEditing = true
            }
        }

        binding.nameEditInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.editNameBtn.performClick()
                true
            } else false
        }
    }

    private fun updateWelcomeText() {
        val name = prefs.userName.ifEmpty { getString(R.string.sensei) }
        binding.welcomeText.text = getString(R.string.hello) + " " + name + "."
    }

    private fun setupButtons() {
        binding.newPostBtn.setOnClickListener {
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.switchToTab(R.id.nav_compose)
        }

        binding.configBtn.setOnClickListener {
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.switchToTab(R.id.nav_menu)
        }

        binding.manageBtn.setOnClickListener {
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.switchToTab(R.id.nav_menu)
        }

        binding.emptyChannels.setOnClickListener {
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.switchToTab(R.id.nav_menu)
        }
    }

    private fun refreshData() {
        // Bot badge
        val botUsername = prefs.botUsername
        if (botUsername.isNotEmpty()) {
            binding.botBadge.text = "@$botUsername"
            binding.botBadge.visibility = View.VISIBLE
        } else {
            binding.botBadge.visibility = View.GONE
        }

        // Channels
        val chats = prefs.getChats()
        channelAdapter.submitList(chats)

        if (chats.isEmpty()) {
            binding.channelsRecycler.visibility = View.GONE
            binding.emptyChannels.visibility = View.VISIBLE
        } else {
            binding.channelsRecycler.visibility = View.VISIBLE
            binding.emptyChannels.visibility = View.GONE
        }

        // Subtitle
        binding.subtitleText.text = getString(R.string.home_subtitle, chats.size)

        // Drafts
        val drafts = prefs.getDrafts()
        if (drafts.isNotEmpty()) {
            binding.draftsHeader.visibility = View.VISIBLE
            draftAdapter.submitList(drafts.take(5))
        } else {
            binding.draftsHeader.visibility = View.GONE
            draftAdapter.submitList(emptyList())
        }
    }

    private fun setupNetworkMonitor() {
        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                activity?.runOnUiThread {
                    binding.offlineBanner.visibility = View.GONE
                }
            }

            override fun onLost(network: Network) {
                activity?.runOnUiThread {
                    binding.offlineBanner.visibility = View.VISIBLE
                }
            }
        }
        networkCallback = callback

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager?.registerNetworkCallback(request, callback)

        // Check initial state
        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        binding.offlineBanner.visibility = if (isConnected) View.GONE else View.VISIBLE
    }

    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        networkCallback = null
        _binding = null
    }
}
