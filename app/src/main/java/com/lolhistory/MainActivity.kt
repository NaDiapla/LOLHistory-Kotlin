package com.lolhistory

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.lolhistory.databinding.ActivityMainBinding
import com.lolhistory.datamodel.SummonerRankInfo
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var viewModel: MainActivityViewModel

    lateinit var inputMethodManager: InputMethodManager

    private var isVisibleInfoLayout = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        viewModel.getSummonerIDInfoLiveData().observe(this, { summonerIDInfo ->
                if (summonerIDInfo == null) {
                    val notExistToast = Toast.makeText(
                        applicationContext,
                        R.string.not_exist_summoner, Toast.LENGTH_SHORT
                    )
                    notExistToast.show()
                    binding.loading.visibility = View.GONE
                }
            })

        viewModel.getSummonerRankInfoLiveData().observe(this, { summonerRankInfo ->
            if (summonerRankInfo != null) {
                binding.inputLayout.visibility = View.GONE
                isVisibleInfoLayout = true
                setRankInfo(summonerRankInfo)
            } else {
                binding.loading.visibility = View.GONE
            }
        })

        viewModel.getHistoryAdapterLiveData().observe(this, { historyAdapter ->
            if (historyAdapter == null) {
                val historyErrorToast = Toast.makeText(
                    applicationContext,
                    R.string.history_error, Toast.LENGTH_SHORT
                )
                historyErrorToast.show()
            } else {
                binding.rvHistory.adapter = historyAdapter
                binding.swipeLayout.isRefreshing = false
            }
            binding.loading.visibility = View.GONE
        })

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.setHasFixedSize(true)

        binding.swipeLayout.setOnRefreshListener { ->
            viewModel.searchSummoner(binding.tvSummonerName.text.toString())
        }

        binding.btnInputSummoner.setOnClickListener { v ->
            binding.loading.visibility = View.VISIBLE
            inputMethodManager.hideSoftInputFromWindow(binding.etInputSummoner.windowToken, 0)
            viewModel.searchSummoner(binding.etInputSummoner.text.toString())
            binding.etInputSummoner.setText("")
        }

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onBackPressed() {
        if (isVisibleInfoLayout) {
            // ?????? ??? ?????? ???
            binding.infoLayout.visibility = View.GONE
            binding.inputLayout.visibility = View.VISIBLE
            isVisibleInfoLayout = !isVisibleInfoLayout
        } else {
            // ?????? ??? ?????? ???
            finish()
        }
    }

    private fun setRankInfo(summonerRankInfo: SummonerRankInfo) {
        setTierEmblem(summonerRankInfo.tier)
        binding.tvSummonerName.text = summonerRankInfo.summonerName
        val tierRank = summonerRankInfo.tier + " " + summonerRankInfo.rank
        binding.tvTier.text = tierRank
        if (summonerRankInfo.tier == "UNRANKED") {
            // ??????
            binding.tvRankType.text = ""
            binding.tvLp.text = ""
            binding.tvTotalWinRate.text = ""
            binding.tvTotalWinLose.text = ""
        } else {
            // ?????? ???
            binding.tvRankType.text = summonerRankInfo.queueType
            val point = summonerRankInfo.leaguePoints.toString() + "LP"
            binding.tvLp.text = point
            val rate = summonerRankInfo.wins.toDouble() / (summonerRankInfo.wins + summonerRankInfo.losses).toDouble() * 100
            binding.tvTotalWinRate.text = String.format(Locale.getDefault(), "%.2f%%", rate)
            val winAndLosses = (summonerRankInfo.wins.toString() + resources.getString(R.string.win) + " " // n???
                    + summonerRankInfo.losses.toString() + resources.getString(R.string.defeat)) // n???
            binding.tvTotalWinLose.text = winAndLosses
        }
        binding.infoLayout.visibility = View.VISIBLE
    }

    private fun setTierEmblem(tier: String) {
        when (tier) {
            "UNRANKED" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_unranked)
            "IRON" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_iron)
            "BRONZE" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_bronze)
            "SILVER" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_silver)
            "GOLD" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_gold)
            "PLATINUM" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_platinum)
            "DIAMOND" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_diamond)
            "MASTER" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_master)
            "GRANDMASTER" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_grandmaster)
            "CHALLENGER" -> binding.ivTierEmblem.setImageResource(R.drawable.emblem_challenger)
        }
    }
}