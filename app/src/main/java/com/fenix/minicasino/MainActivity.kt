package com.fenix.minicasino

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
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

    // Botón girar de la ruleta
    private lateinit var btnGirar: Button

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
        btn.background = crearFondoRedondeado(colorAcento)
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
        btn.background = crearFondoRedondeado(Color.TRANSPARENT, strokeColor = colorDorado, strokeWidth = 3)
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

        val ruletaView = RuletaView(this)

        // Se asigna a la variable de clase btnGirar
        btnGirar = crearBoton("Girar", View.OnClickListener {
            if (puntosUsuario < 10) {
                Toast.makeText(this, "Sin puntos suficientes", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            // Desactivar botón durante la animación
            btnGirar.isEnabled = false
            puntosUsuario -= 10
            actualizarPuntos()

            val indice = random.nextInt(8) // 0..7
            val multiplicadores = arrayOf(0, 0, 0, 2, 2, 3, 5, 10)
            val mult = multiplicadores[indice]

            ruletaView.girarA(indice) {
                // Callback al terminar la animación
                val ganancia = 10 * mult
                puntosUsuario += ganancia
                guardarPuntos()
                actualizarPuntos()
                tvResultado.text = "Resultado: x$mult"
                Toast.makeText(this, "¡Obtuviste x$mult!", Toast.LENGTH_SHORT).show()
                btnGirar.isEnabled = true
            }
        })

        // LayoutParams para la ruleta (peso 1 para ocupar el espacio restante)
        val lpRuleta = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        ruletaView.layoutParams = lpRuleta

        layout.addView(tvResultado)
        layout.addView(ruletaView)
        layout.addView(btnGirar)
        layout.addView(crearBotonVolver())

        contenedorJuegos.addView(layout)
    }

    // Clase personalizada para dibujar la ruleta
    private inner class RuletaView(context: Context) : View(context) {

        private var rotationAngle = 0f
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val rect = RectF()
        private val sectorCount = 8
        private val sectorAngle = 360f / sectorCount
        private val multiplicadores = arrayOf(0, 0, 0, 2, 2, 3, 5, 10)

        // Colores
        private val colorRojo = Color.parseColor("#C41E3A")
        private val colorAzul = Color.parseColor("#1B2B4D")
        private val colorBorde = Color.parseColor("#D4AF37")

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            val padding = 40f
            rect.set(padding, padding, w - padding, h - padding)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Dibujar la rueda rotada
            canvas.save()
            canvas.rotate(rotationAngle, width / 2f, height / 2f)

            for (i in 0 until sectorCount) {
                // Fondo del sector
                paint.style = Paint.Style.FILL
                paint.color = if (i % 2 == 0) colorRojo else colorAzul
                canvas.drawArc(rect, i * sectorAngle, sectorAngle, true, paint)

                // Borde dorado
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 8f
                paint.color = colorBorde
                canvas.drawArc(rect, i * sectorAngle, sectorAngle, true, paint)

                // Texto del multiplicador
                paint.style = Paint.Style.FILL
                paint.color = Color.WHITE
                paint.textSize = (rect.width() / 8)
                paint.textAlign = Paint.Align.CENTER

                // Calcular posición del texto en el centro del sector
                val angle = (i + 0.5f) * sectorAngle
                val radius = rect.width() / 2f * 0.6f
                val cx = width / 2f + radius * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
                val cy = height / 2f + radius * Math.sin(Math.toRadians(angle.toDouble())).toFloat() + paint.textSize / 3f
                canvas.drawText("x${multiplicadores[i]}", cx, cy, paint)
            }

            canvas.restore()

            // Dibujar la flecha dorada fija en la parte superior
            val arrowPath = Path()
            val arrowHeight = 60f
            val arrowWidth = 40f
            val centerX = width / 2f
            val topY = rect.top - arrowHeight / 2f
            arrowPath.moveTo(centerX, topY) // punta
            arrowPath.lineTo(centerX - arrowWidth / 2f, topY + arrowHeight)
            arrowPath.lineTo(centerX + arrowWidth / 2f, topY + arrowHeight)
            arrowPath.close()
            paint.style = Paint.Style.FILL
            paint.color = colorBorde
            canvas.drawPath(arrowPath, paint)
        }

        fun girarA(sector: Int, onEnd: (Int) -> Unit) {
            val start = rotationAngle
            // 4 vueltas completas = 1440°
            val vueltas = 1440f
            val extra = sector * sectorAngle
            val animator = ValueAnimator.ofFloat(start, start + vueltas + extra)
            animator.duration = 3000
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener {
                rotationAngle = (it.animatedValue as Float) % 360f
                invalidate()
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rotationAngle = (start + vueltas + extra) % 360f
                    invalidate()
                    onEnd(sector)
                }
            })
            animator.start()
        }
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
                tv.background = crearFondoRedondeado(colorFondoClaro, strokeColor = colorDorado, strokeWidth = 3)
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
        tvJugador.background = crearFondoRedondeado(colorFondoClaro, strokeColor = colorDorado, strokeWidth = 3)
        tvJugador.setPadding(16, 16, 16, 16)

        val tvBanca = TextView(this)
        tvBanca.text = "Banca: "
        tvBanca.setTextColor(Color.WHITE)
        tvBanca.background = crearFondoRedondeado(colorFondoClaro, strokeColor = colorDorado, strokeWidth = 3)
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