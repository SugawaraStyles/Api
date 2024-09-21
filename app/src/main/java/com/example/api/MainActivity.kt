package com.example.api

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.api.databinding.ActivityMainBinding
import com.example.api.doglist.APIService
import com.example.api.doglist.DogAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: DogAdapter
    private val dogImages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar SearchView para recibir eventos
        binding.svDogs.setOnQueryTextListener(this)

        // Inicializar el RecyclerView
        initRecyclerView()
    }

    private fun initRecyclerView() {
        // Configurar el adaptador para el RecyclerView
        adapter = DogAdapter(dogImages)
        binding.rvDogs.layoutManager = LinearLayoutManager(this)
        binding.rvDogs.adapter = adapter
    }

    private fun getRetrofit(): Retrofit {
        // Retorna la instancia de Retrofit para llamadas HTTP
        return Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/breed/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun showError() {
        // Muestra un mensaje en caso de error
        Toast.makeText(this, "Ha ocurrido un error", Toast.LENGTH_LONG).show()
    }

    private fun searchByName(query: String) {
        // Llama a la API en un hilo separado usando corrutinas
        CoroutineScope(Dispatchers.IO).launch {
            val call: Response<DogsResponse> = getRetrofit().create(APIService::class.java)
                .getDogsByBreeds("$query/images")
            val puppies: DogsResponse? = call.body()

            runOnUiThread {
                if (call.isSuccessful) {
                    val images = puppies?.images ?: emptyList()
                    dogImages.clear()
                    dogImages.addAll(images)
                    adapter.notifyDataSetChanged()
                } else {
                    showError()
                }
            }
        }
    }

    // Método que se invoca cuando el usuario envía la búsqueda
    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrEmpty()) {
            searchByName(query.toLowerCase())  // Realiza la búsqueda
        }
        return true
    }

    // Método que se invoca cuando cambia el texto de búsqueda
    override fun onQueryTextChange(newText: String?): Boolean {
        // No hace nada mientras se cambia el texto
        return true
    }
}
