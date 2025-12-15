package com.example.nt2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 1. プロフィール設定ボタンの機能
        val profileSettingButton: TextView = view.findViewById(R.id.menu_my_profile)
        profileSettingButton.setOnClickListener {
            navigateToProfiSetting()
        }

        // 2. ログアウトボタンの機能
        val logoutButton: Button = view.findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return view
    }

    // プロフィール設定画面へ遷移する関数
    private fun navigateToProfiSetting() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProfiSetting())
            .addToBackStack(null) // 戻るボタンで戻れるようにスタックに追加
            .commit()
    }

    // ログアウト確認ダイアログを表示する関数
    private fun showLogoutConfirmationDialog() {
        // AlertDialog.Builderを使用して確認ダイアログを作成します。
        AlertDialog.Builder(requireContext())
            .setTitle("ログアウトの確認")
            .setMessage("本当にこのアカウントからログアウトしますか？")
            .setPositiveButton("ログアウト") { dialog, which ->
                // ログアウト処理を実行
                performLogout()
            }
            .setNegativeButton("キャンセル") { dialog, which ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "ログアウトをキャンセルしました", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // 実際のログアウト処理（ここではシミュレーション）
    private fun performLogout() {
        // ここにFirebase Authなど、実際のログアウト処理を記述します。
        Toast.makeText(requireContext(), "ログアウトしました", Toast.LENGTH_LONG).show()
        // ログアウト後、ログイン画面へ戻るなどの処理を実装します。
    }
}