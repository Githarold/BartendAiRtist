/**
 * IngredientAdapter.kt
 * 재료 목록을 표시하는 데 사용되는 RecyclerView와 관련된 어댑터 클래스
 * 재료의 이름과 수량을 표시하는 뷰 홀더 클래스와 그 뷰 홀더를 생성하고 바인딩하는 메서드가 포함되어 있다
 */

package com.example.project

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID

class IngredientAdapter(private val ingredients: List<Ingredient>) : RecyclerView.Adapter<IngredientAdapter.ViewHolder>() {
    private val REQUEST_ENABLE_BT = 1
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 100
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val SERVER_DEVICE_ADDRESS = "DC:A6:32:7B:04:EC"  // 서버 기기의 MAC 주소를 입력해야 합니다. 라즈베리파이
    private val SERVER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")  // SPP UUID
    private lateinit var bluetoothSocket: BluetoothSocket
    private var isCommunicating = false
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ingredient = ingredients[position]
        holder.nameTextView.text = ingredient.name
        holder.quantityTextView.text = ingredient.quantity.toString()

        holder.plusButton.setOnClickListener {
            ingredient.quantity++
            notifyItemChanged(position)
        }

        holder.minusButton.setOnClickListener {
            if (ingredient.quantity > 0) {
                ingredient.quantity--
                notifyItemChanged(position)
            }
        }
    }

    override fun getItemCount() = ingredients.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.ingredientName)
        val quantityTextView: TextView = itemView.findViewById(R.id.how_many)
        val plusButton: Button = itemView.findViewById(R.id.plus)
        val minusButton: Button = itemView.findViewById(R.id.minus)
    }
}

