package com.senseigram.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.senseigram.R
import com.senseigram.SenseiGramApp
import com.senseigram.data.model.SavedChat
import com.senseigram.ui.adapters.SavedChatsAdapter
import com.senseigram.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: SavedChatsAdapter
    
    companion object {
        fun newInstance() = HomeFragment()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView(view)
        observeData(view)
    }
    
    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvSavedChats)
        adapter = SavedChatsAdapter(
            onItemClick = { chat ->
                openCompose(chat)
            },
            onDeleteClick = { chat ->
                viewModel.removeSavedChat(chat.id)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }
    
    private fun observeData(view: View) {
        viewModel.savedChats.observe(viewLifecycleOwner) { chats ->
            adapter.submitList(chats)
            
            val emptyView = view.findViewById<TextView>(R.id.tvEmpty)
            if (chats.isEmpty()) {
                emptyView.visibility = View.VISIBLE
            } else {
                emptyView.visibility = View.GONE
            }
        }
        
        viewModel.botUser.observe(viewLifecycleOwner) { user ->
            val tvBotInfo = view.findViewById<TextView>(R.id.tvBotInfo)
            if (user != null) {
                tvBotInfo.text = "@${user.username ?: user.first_name}"
            } else {
                tvBotInfo.text = "Not connected"
            }
        }
    }
    
    private fun openCompose(chat: SavedChat) {
        lifecycleScope.launch {
            val token = SenseiGramApp.preferenceManager.botToken.first()
            if (token.isEmpty()) {
                return@launch
            }
            val fragment = ComposeFragment.newInstance(chat.id.toString())
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
