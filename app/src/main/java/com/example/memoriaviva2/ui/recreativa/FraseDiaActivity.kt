package com.example.memoriaviva2.ui.recreativa

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.memoriaviva2.R
import java.util.*

class FraseDiaActivity : AppCompatActivity() {
    
    private lateinit var textoFrase: TextView
    private lateinit var btnProxima: Button

    
    private val frasesDoDia = listOf(
        "Hoje é um novo dia, aproveite cada momento!",
        "Um sorriso pode iluminar o seu dia.",
        "Lembre-se: cada pequeno passo conta.",
        "Você é importante e amado.",
        "Aproveite os sons e cheiros ao seu redor.",
        "Respire fundo e sinta-se tranquilo.",
        "Cada dia é uma nova oportunidade.",
        "Faça algo que te faça feliz hoje.",
        "A vida é feita de pequenos momentos.",
        "Você é capaz de coisas incríveis.",
        "Um abraço pode mudar o seu humor.",
        "A música traz lembranças boas, ouça uma canção.",
        "Sorria, mesmo sem motivo, isso ajuda o coração.",
        "Cada lembrança feliz vale ouro.",
        "Observe a beleza das pequenas coisas.",
        "Hoje você pode aprender algo novo.",
        "Um dia de cada vez, com calma.",
        "Compartilhe alegria com quem está perto.",
        "A gratidão transforma o dia.",
        "Pense em algo que te faça feliz.",
        "Um raio de sol aquece a alma.",
        "O carinho é uma forma de cuidado.",
        "Cada gesto de bondade conta.",
        "Valorize o que você já conquistou.",
        "Respire fundo e sorria.",
        "Aproveite a companhia de quem ama.",
        "Lembre-se: você é capaz.",
        "Momentos simples podem ser mágicos.",
        "Hoje é um bom dia para agradecer.",
        "Faça algo criativo, mesmo pequeno.",
        "Cada memória feliz é um tesouro.",
        "Você faz diferença na vida de alguém.",
        "Uma pausa para respirar é importante.",
        "Deixe a mente relaxar por alguns minutos.",
        "O mundo tem coisas bonitas, observe-as.",
        "Ria de si mesmo, é libertador.",
        "Compartilhar histórias traz alegria.",
        "A paciência é amiga da tranquilidade.",
        "Aprecie o momento presente.",
        "Pequenos passos levam a grandes conquistas.",
        "Seja gentil consigo mesmo.",
        "Cada dia traz uma nova chance.",
        "Faça algo por alguém hoje.",
        "Sinta o vento no rosto e sorria.",
        "Momentos felizes podem ser simples.",
        "O carinho transforma corações.",
        "Ouça sua música favorita e dance.",
        "Olhe para o céu e respire fundo.",
        "Valorize cada pequeno gesto de amor.",
        "Seja grato pelas coisas simples.",
        "Hoje é um dia para novas memórias.",
        "Divida seu sorriso com alguém.",
        "Cuide de si com amor e paciência.",
        "Um minuto de silêncio pode acalmar a mente.",
        "Faça uma caminhada curta e observe a natureza.",
        "Ria das pequenas coisas da vida.",
        "Cada momento de alegria é importante.",
        "Um gesto de bondade muda o dia de alguém.",
        "Compartilhe lembranças felizes com os outros.",
        "Ouça uma história que te faça sorrir.",
        "Faça algo que você goste, mesmo simples.",
        "Valorize os pequenos prazeres.",
        "Sinta a textura de algo que gosta.",
        "Um pensamento positivo muda a perspectiva.",
        "Hoje é um dia para tentar algo novo.",
        "Conte uma memória feliz para alguém.",
        "Um abraço é sempre bem-vindo.",
        "Aprenda algo simples e divertido hoje.",
        "Observe o mundo com curiosidade.",
        "Compartilhe um momento de alegria.",
        "Seja paciente consigo mesmo.",
        "Olhe para algo bonito e sorria.",
        "Faça uma pequena gentileza hoje.",
        "Respire fundo e se conecte com o presente.",
        "Um pensamento feliz ilumina o dia.",
        "Escute o canto dos pássaros.",
        "Brinque com cores e formas.",
        "Ouça sons que te acalmem.",
        "Seja curioso e explore.",
        "Compartilhe uma risada hoje.",
        "Valorize o que você tem agora.",
        "Faça algo que te traga conforto.",
        "Sorria ao lembrar de algo bom.",
        "Cuide do seu coração com carinho.",
        "Observe o movimento das nuvens.",
        "Um pequeno gesto de amor vale muito.",
        "Compartilhe suas histórias favoritas.",
        "Hoje é um bom dia para recomeçar.",
        "Sinta gratidão pelo que tem.",
        "Observe o brilho de algo simples.",
        "Ria com alguém que gosta de você.",
        "Faça algo gentil por si mesmo.",
        "Respire profundamente e sorria.",
        "Um momento de silêncio traz paz.",
        "Escute sons que tragam boas lembranças.",
        "Compartilhe um pensamento positivo.",
        "Valorize suas pequenas conquistas.",
        "Hoje é um dia para celebrar você.",
        "Sinta alegria nas coisas simples.",
        "Ria das memórias engraçadas.",
        "Compartilhe um momento especial.",
        "Faça algo que te faça sentir bem.",
        "Olhe para o horizonte e inspire-se.",
        "Respire fundo e acalme a mente.",
        "Um gesto de amor muda o mundo.",
        "Sinta o calor do sol na pele.",
        "Compartilhe carinho com alguém.",
        "Ria sem motivo, é libertador.",
        "Valorize cada pequeno instante.",
        "Hoje é um dia para sorrir.",
        "Observe algo que te faz feliz.",
        "Faça algo criativo, mesmo simples.",
        "Compartilhe boas lembranças.",
        "Respire profundamente e sinta paz.",
        "Um momento de alegria ilumina o dia.",
        "Ria das pequenas coisas da vida.",
        "Cuide de si com carinho e paciência.",
        "Observe a beleza em volta.",
        "Compartilhe amor e gentileza.",
        "Faça algo por alguém, mesmo simples.",
        "Respire fundo e se conecte com o momento.",
        "Valorize pequenas conquistas diárias.",
        "Hoje é um dia para novos começos.",
        "Ria com alguém que gosta de você.",
        "Olhe para o céu e inspire esperança.",
        "Compartilhe uma lembrança feliz.",
        "Faça algo que te traga conforto.",
        "Seja gentil consigo mesmo hoje.",
        "Um minuto de silêncio acalma a mente.",
        "Respire profundamente e sorria.",
        "Compartilhe alegria com os outros.",
        "Valorize os pequenos momentos de felicidade.",
        "Hoje é um dia para gratidão.",
        "Ria das memórias boas.",
        "Faça algo simples e divertido.",
        "Observe o mundo ao seu redor.",
        "Compartilhe um sorriso sincero.",
        "Sinta alegria nas pequenas coisas.",
        "Ria das pequenas situações engraçadas.",
        "Faça algo que te faça feliz agora.",
        "Olhe para algo bonito e sorria.",
        "Compartilhe carinho e gentileza.",
        "Valorize cada instante do dia.",
        "Respire fundo e aproveite o momento.",
        "Hoje é um dia para novas experiências.",
        "Ria e compartilhe alegria.",
        "Faça algo por alguém e sinta felicidade.",
        "Observe pequenas belezas ao redor.",
        "Sinta gratidão pelo que tem agora.",
        "Compartilhe memórias felizes.",
        "Ria de algo engraçado da vida.",
        "Faça algo que te acalme e sorria.",
        "Olhe para o céu e inspire-se.",
        "Compartilhe amor e gentileza.",
        "Valorize pequenas conquistas.",
        "Respire profundamente e relaxe.",
        "Hoje é um dia para sorrir de coração.",
        "Ria das lembranças felizes.",
        "Faça algo criativo e simples.",
        "Observe a natureza ao redor.",
        "Compartilhe momentos especiais.",
        "Sinta alegria nas pequenas coisas.",
        "Ria sem motivo e contagie outros.",
        "Faça algo que te faça bem.",
        "Olhe para algo bonito e se inspire.",
        "Compartilhe um gesto de carinho.",
        "Valorize cada pequeno instante.",
        "Respire fundo e sinta paz.",
        "Hoje é um dia para gratidão e alegria.",
        "Ria das pequenas situações engraçadas.",
        "Faça algo simples e prazeroso.",
        "Observe o movimento das nuvens.",
        "Compartilhe amor com alguém.",
        "Sinta alegria no presente.",
        "Ria das memórias engraçadas.",
        "Faça algo criativo hoje.",
        "Olhe para o horizonte e sorria.",
        "Compartilhe uma lembrança feliz.",
        "Valorize cada instante do dia.",
        "Respire fundo e se conecte com o momento.",
        "Hoje é um dia para novos começos.",
        "Ria com alguém que gosta de você.",
        "Faça algo que te traga conforto.",
        "Observe a beleza das pequenas coisas.",
        "Compartilhe alegria com os outros.",
        "Valorize pequenas conquistas diárias.",
        "Respire profundamente e sorria.",
        "Um gesto de amor muda o dia.",
        "Sinta o calor do sol na pele.",
        "Compartilhe carinho e gentileza.",
        "Ria sem motivo e contagie os outros.",
        "Valorize cada momento do dia.",
        "Hoje é um dia para sorrir de coração.",
        "Observe algo bonito ao redor.",
        "Faça algo simples e prazeroso.",
        "Compartilhe memórias felizes.",
        "Sinta alegria nas pequenas coisas.",
        "Ria das lembranças engraçadas.",
        "Faça algo criativo e divertido.",
        "Olhe para o céu e inspire-se.",
        "Compartilhe amor com alguém próximo.",
        "Valorize cada instante do dia.",
        "Respire fundo e se conecte com o presente.",
        "Hoje é um dia para gratidão e alegria.",
        "Ria das pequenas coisas e sorria.",
        "Faça algo que te faça feliz agora.",
        "Observe o mundo ao redor com curiosidade.",
        "Compartilhe momentos especiais com alguém.",
        "Sinta alegria no presente momento.",
        "Ria sem motivo e contagie outros.",
        "Faça algo criativo e simples hoje.",
        "Olhe para algo bonito e se inspire.",
        "Compartilhe carinho e gentileza.",
        "Valorize cada pequeno instante do dia.",
        "Respire profundamente e relaxe.",
        "Hoje é um dia para sorrir e ser feliz.",
        "Ria das memórias boas e engraçadas.",
        "Faça algo simples e prazeroso agora.",
        "Observe a natureza e sinta paz.",
        "Compartilhe amor e alegria com alguém.",
        "Sinta gratidão pelas pequenas coisas.",
        "Ria das situações engraçadas do dia.",
        "Faça algo criativo que te faça sorrir.",
        "Olhe para o horizonte e inspire-se.",
        "Compartilhe uma lembrança feliz com alguém.",
        "Valorize cada instante do seu dia.",
        "Respire fundo e aproveite o momento.",
        "Hoje é um dia para novas experiências felizes.",
        "Ria com alguém querido e sorria.",
        "Faça algo que te traga conforto e alegria.",
        "Observe a beleza ao redor e sorria.",
        "Compartilhe alegria e gentileza com os outros.",
        "Valorize pequenas conquistas e momentos felizes.",
        "Respire profundamente e sinta tranquilidade.",
        "Um gesto de amor muda o dia de alguém.",
        "Sinta o calor do sol ou da luz ao redor.",
        "Compartilhe carinho com alguém especial.",
        "Ria sem motivo e espalhe alegria.",
        "Valorize cada instante e momento do dia.",
        "Hoje é um dia para sorrir de coração e alegria.",
        "Observe algo bonito e inspire-se.",
        "Faça algo simples, prazeroso e criativo.",
        "Compartilhe memórias e histórias felizes.",
        "Sinta alegria nas pequenas coisas do dia.",
        "Ria das lembranças engraçadas ou boas.",
        "Faça algo divertido e simples agora.",
        "Olhe para o céu, horizonte ou natureza e sorria.",
        "Compartilhe amor, carinho e gentileza com alguém.",
        "Valorize cada momento e instante do seu dia.",
        "Respire fundo e se conecte com o presente momento."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frase_dia)
        

        initViews()
        setupButtons()
        mostrarFraseDoDia()
    }
    
    private fun initViews() {
        textoFrase = findViewById(R.id.textoFrase)
        btnProxima = findViewById(R.id.btnProxima)
        findViewById<Button>(R.id.btnVoltar).setOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        btnProxima.setOnClickListener {
            mostrarProximaFrase()
        }
    }
    
    private fun mostrarFraseDoDia() {
        val fraseDoDia = obterFraseDoDia()
        textoFrase.text = fraseDoDia
    }
    
    private fun mostrarProximaFrase() {
        val proximaFrase = obterProximaFrase()
        textoFrase.text = proximaFrase
    }
    
    private fun obterFraseDoDia(): String {
        val hoje = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val fraseIndex = hoje % frasesDoDia.size
        return frasesDoDia[fraseIndex]
    }
    
    private fun obterProximaFrase(): String {
        return frasesDoDia.random()
    }
}