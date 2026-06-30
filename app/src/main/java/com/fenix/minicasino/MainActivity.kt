package com.fenix.minicasino

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var puntosUsuario: Int = 100

    private lateinit var tvPuntos: TextView
    private lateinit var contenedorJuegos: FrameLayout

    private val random = Random()

    // Colors
    private val colorFondo = Color.parseColor("#0A1633")
    private val colorAcento = Color.parseColor("#C41E3A")
    private val colorDorado = Color.parseColor("#D4AF37")
    private val colorFondoClaro = Color.parseColor("#1B2B4D")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("mini_casino_prefs", Context.MODE_PRIVATE)
        puntosUsuario = prefs.getInt("puntos", 100)

        tvPuntos = findViewById(R.id.tv_puntos)
        contenedorJuegos = findViewById(R.id.contenedor_juegos)

        actualizarPuntos()
        mostrarMenuPrincipal()
    }

    private fun guardarPuntos() {
        prefs.edit().putInt("puntos", puntosUsuario).apply()
    }

    private fun actualizarPuntos() {
        tvPuntos.text = "💰 Puntos: $puntosUsuario"
    }

    private fun crearFondoRedondeado(colorFondo: Int, radio: Float = 12f, strokeColor: Int? = null, strokeWidth: Int = 0): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radio
            setColor(colorFondo)
            if (strokeColor != null && strokeWidth > 0) {
                setStroke(strokeWidth, strokeColor)
            }
        }
    }

    private fun crearBoton(texto: String, onClick: View.OnClickListener): Button {
        val btn = Button(this)
        btn.text = texto
        btn.setTextColor(Color.WHITE)
        btn.setTypeface(null, Typeface.BOLD)
        btn.setBackgroundDrawable(crearFondoRedondeado(colorAcento))
        btn.setOnClickListener(onClick)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 16, 0, 0)
        btn.layoutParams = params
        return btn
    }

    private fun crearBotonVolver(): Button {
        val btn = Button(this)
        btn.text = "← Volver"
        btn.setTextColor(colorDorado)
        btn.setTypeface(null, Typeface.BOLD)
        btn.setBackgroundDrawable(crearFondoRedondeado(Color.TRANSPARENT, strokeColor = colorDorado, strokeWidth = 3))
        btn.setOnClickListener { mostrarMenuPrincipal() }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 24, 0, 0)
        btn.layoutParams = params
        return btn
    }

    private fun mostrarMenuPrincipal() {
        contenedorJuegos.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(colorFondo)
        layout.setPadding(20, 20, 20, 20)

        val btnRuleta = crearBoton("🎰 Ruleta", View.OnClickListener { mostrarRuleta() })
        val btnRasca = crearBoton("🎟️ Rasca y Gana", View.OnClickListener { mostrarRasca() })
        val btnBlackjack = crearBoton("🃏 Blackjack", View.OnClickListener { mostrarBlackjack() })

        layout.addView(btnRuleta)
        layout.addView(btnRasca)
        layout.addView(btnBlackjack)

        contenedorJuegos.addView(layout)
    }

    // ---------- RUETA ----------
    private fun mostrarRuleta() {
        contenedorJuegos.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(colorFondo)
        layout.setPadding(20, 20, 20, 20)

        val tvResultado = TextView(this)
        tvResultado.text = "Resultado: -"
        tvResultado.setTextColor(colorDorado)
        tvResultado.setTypeface(null, Typeface.BOLD)
        tvResultado.textSize = 24f
        tvResultado.gravity = Gravity.CENTER
        val lpResult = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lpResult.setMargins(0, 0, 0, 24)
        tvResultado.layoutParams = lpResult

        val btnGirar = crearBoton("Girar", View.OnClickListener {
            if (puntosUsuario < 10) {
                Toast.makeText(this, "Sin puntos suficientes", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            puntosUsuario -= 10
            val indice = random.nextInt(8)
            val multiplicadores = arrayOf(0, 0, 0, 2, 2, 3, 5, 10)
            val mult = multiplicadores[indice]
            val ganancia = 10 * mult
            puntosUsuario += ganancia
            guardarPuntos()
            actualizarPuntos()
            tvResultado.text = "Resultado: x$mult"
            Toast.makeText(this, "¡Obtuviste x$mult!", Toast.LENGTH_SHORT).show()
        })

        layout.addView(tvResultado)
        layout.addView(btnGirar)
        layout.addView(crearBotonVolver())

        contenedorJuegos.addView(layout)
    }

    // ---------- RASCA ----------
    private fun mostrarRasca() {
        contenedorJuegos.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(colorFondo)
        layout.setPadding(20, 20, 20, 20)

        val tvSimbolos = LinearLayout(this)
        tvSimbolos.orientation = LinearLayout.HORIZONTAL
        tvSimbolos.gravity = Gravity.CENTER

        val simbolos = arrayOf("🍒", "🍋", "🔔", "💎", "7️⃣")

        val btnComprar = crearBoton("Comprar carta", View.OnClickListener {
            if (puntosUsuario < 5) {
                Toast.makeText(this, "Sin puntos suficientes", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            puntosUsuario -= 5
            val s1 = simbolos[random.nextInt(simbolos.size)]
            val s2 = simbolos[random.nextInt(simbolos.size)]
            val s3 = simbolos[random.nextInt(simbolos.size)]

            tvSimbolos.removeAllViews()
            val lista = listOf(s1, s2, s3)
            for (s in lista) {
                val tv = TextView(this)
                tv.text = s
                tv.textSize = 32f
                tv.gravity = Gravity.CENTER
                tv.setTextColor(Color.WHITE)
                tv.setBackgroundDrawable(crearFondoRedondeado(colorFondoClaro, strokeColor = colorDorado, strokeWidth = 3))
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                params.setMargins(8, 8, 8, 8)
                tv.layoutParams = params
                tvSimbolos.addView(tv)
            }

            // Calcular premio
            val premio = when {
                s1 == s2 && s2 == s3 -> 50
                s1 == s2 || s1 == s3 || s2 == s3 -> 15
                else -> 0
            }
            if (premio > 0) {
                puntosUsuario += premio
                Toast.makeText(this, "¡Ganaste $premio puntos!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No coincidencias, mejor suerte la próxima.", Toast.LENGTH_SHORT).show()
            }
            guardarPuntos()
            actualizarPuntos()
        })

        layout.addView(tvSimbolos)
        layout.addView(btnComprar)
        layout.addView(crearBotonVolver())

        contenedorJuegos.addView(layout)
    }

    // ---------- BLACKJACK ----------
    private var jugadorCartas = mutableListOf<Int>()
    private var bancaCartas = mutableListOf<Int>()
    private var juegoActivo = false

    private fun mostrarBlackjack() {
        contenedorJuegos.removeAllViews()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setBackgroundColor(colorFondo)
        layout.setPadding(20, 20, 20, 20)

        val tvJugador = TextView(this)
        tvJugador.text = "Jugador: "
        tvJugador.setTextColor(Color.WHITE)
        tvJugador.setBackgroundDrawable(crearFondoRedondeado(colorFondoClaro, strokeColor = colorDorado, strokeWidth = 3))
        tvJugador.setPadding(16, 16, 16, 16)

        val tvBanca = TextView(this)
        tvBanca.text = "Banca: "
        tvBanca.setTextColor(Color.WHITE)
        tvBanca.setBackgroundDrawable(crearFondoRedondeado(colorFondoClaro, strokeColor = colorDorado, strokeWidth = 3))
        tvBanca.setPadding(16, 16, 16, 16)

        val btnRepartir = crearBoton("Repartir", View.OnClickListener {
            if (puntosUsuario < 10) {
                Toast.makeText(this, "Sin puntos suficientes", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            puntosUsuario -= 10
            guardarPuntos()
            actualizarPuntos()
            iniciarPartida(tvJugador, tvBanca)
        })

        val btnPedir = crearBoton("Pedir carta", View.OnClickListener {
            if (!juegoActivo) return@OnClickListener
            val carta = random.nextInt(11) + 1
            jugadorCartas.add(carta)
            actualizarManos(tvJugador, tvBanca)
            val total = jugadorCartas.sum()
            if (total > 21) {
                terminarPartida(false, tvJugador, tvBanca)
            }
        })

        val btnPlantarse = crearBoton("Plantarse", View.OnClickListener {
            if (!juegoActivo) return@OnClickListener
            // Revelar banca y jugar
            while (bancaCartas.sum() < 17) {
                bancaCartas.add(random.nextInt(11) + 1)
            }
            actualizarManos(tvJugador, tvBanca, revelarBanca = true)
            val totalJugador = jugadorCartas.sum()
            val totalBanca = bancaCartas.sum()
            val ganaJugador = when {
                totalBanca > 21 -> true
                totalJugador > totalBanca -> true
                totalJugador == totalBanca -> null // empate
                else -> false
            }
            when (ganaJugador) {
                true -> {
                    puntosUsuario += 20
                    Toast.makeText(this, "¡Ganaste! +20 puntos", Toast.LENGTH_SHORT).show()
                }
                false -> {
                    Toast.makeText(this, "Perdiste la apuesta", Toast.LENGTH_SHORT).show()
                }
                null -> {
                    puntosUsuario += 10 // recupera apuesta
                    Toast.makeText(this, "Empate, apuesta recuperada", Toast.LENGTH_SHORT).show()
                }
            }
            terminarPartida(ganaJugador == true, tvJugador, tvBanca)
        })

        layout.addView(tvJugador)
        layout.addView(tvBanca)
        layout.addView(btnRepartir)
        layout.addView(btnPedir)
        layout.addView(btnPlantarse)
        layout.addView(crearBotonVolver())

        contenedorJuegos.addView(layout)
    }

    private fun iniciarPartida(tvJugador: TextView, tvBanca: TextView) {
        juegoActivo = true
        jugadorCartas.clear()
        bancaCartas.clear()
        // Dos cartas cada uno
        jugadorCartas.add(random.nextInt(11) + 1)
        jugadorCartas.add(random.nextInt(11) + 1)
        bancaCartas.add(random.nextInt(11) + 1)
        bancaCartas.add(random.nextInt(11) + 1)
        actualizarManos(tvJugador, tvBanca, revelarBanca = false)
        // Verificar bust inmediato
        if (jugadorCartas.sum() > 21) {
            terminarPartida(false, tvJugador, tvBanca)
        }
    }

    private fun actualizarManos(tvJugador: TextView, tvBanca: TextView, revelarBanca: Boolean = false) {
        tvJugador.text = "Jugador: ${jugadorCartas.joinToString(", ")} (Total: ${jugadorCartas.sum()})"
        if (revelarBanca) {
            tvBanca.text = "Banca: ${bancaCartas.joinToString(", ")} (Total: ${bancaCartas.sum()})"
        } else {
            val primeraCarta = bancaCartas[0]
            tvBanca.text = "Banca: $primeraCarta, ?"
        }
    }

    private fun terminarPartida(gano: Boolean, tvJugador: TextView, tvBanca: TextView) {
        juegoActivo = false
        actualizarManos(tvJugador, tvBanca, revelarBanca = true)
        guardarPuntos()
        actualizarPuntos()
    }
}