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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.senseigram.R
import com.senseigram.data.Prefs
import com.senseigram.databinding.FragmentHomeBinding
import com.senseigram.ui.adapters.ChannelGridAdapter
import com.senseigram.ui.main.MainActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var prefs: Prefs
    private lateinit var channelAdapter: ChannelGridAdapter

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())

        setupAdapter()
        setupButtons()
        setupNetworkMonitor()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupAdapter() {
        channelAdapter = ChannelGridAdapter { chat ->
            val activity = requireActivity() as? MainActivity ?: return@ChannelGridAdapter
            activity.switchToCompose()
        }
        binding.channelsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.channelsRecycler.adapter = channelAdapter
    }

    private fun setupButtons() {
        binding.newPostCard.setOnClickListener {
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.switchToCompose()
        }

        binding.draftsCard.setOnClickListener {
            val activity = requireActivity() as? MainActivity ?: return@setOnClickListener
            activity.switchToCompose()
        }

        binding.emptyChannels.setOnClickListener {
            // No action needed
        }
    }

    private fun refreshData() {
        val name = prefs.userName.ifEmpty { getString(R.string.sensei) }
        binding.welcomeText.text = getString(R.string.hello) + ", " + name

        val botUsername = prefs.botUsername
        if (botUsername.isNotEmpty()) {
            binding.botBadge.text = "@$botUsername"
            binding.botBadge.visibility = View.VISIBLE
        } else {
            binding.botBadge.visibility = View.GONE
        }

        val chats = prefs.getChats()
        channelAdapter.submitList(chats)

        if (chats.isEmpty()) {
            binding.channelsRecycler.visibility = View.GONE
            binding.emptyChannels.visibility = View.VISIBLE
        } else {
            binding.channelsRecycler.visibility = View.VISIBLE
            binding.emptyChannels.visibility = View.GONE
        }

        binding.subtitleText.text = "${chats.size} channels connected"
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

        val activeNetwork = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        binding.offlineBanner.visibility = if (isConnected) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        networkCallback = null
        _binding = null
    }
}
