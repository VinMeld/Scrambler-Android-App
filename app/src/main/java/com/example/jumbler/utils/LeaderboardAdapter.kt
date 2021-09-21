package com.example.jumbler.utils

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import com.example.jumbler.R
import android.widget.TextView
import java.lang.StringBuilder
import java.util.ArrayList

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {
    private var rankings: ArrayList<LeaderboardItem> = ArrayList<LeaderboardItem>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ranking.text =
            StringBuilder().append(rankings[position].ranking).append(". ").toString()
        holder.username.text = rankings[position].username
    }

    override fun getItemCount(): Int {
        return rankings.size
    }

    fun setRankings(rankings: ArrayList<LeaderboardItem>) {
        this.rankings = rankings
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ranking: TextView = itemView.findViewById(R.id.leaderboard_ranking)
        val username: TextView = itemView.findViewById(R.id.leaderboard_username)
    }
}