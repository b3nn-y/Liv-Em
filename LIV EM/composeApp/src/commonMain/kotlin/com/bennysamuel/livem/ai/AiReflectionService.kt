package com.bennysamuel.livem.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.bennysamuel.livem.db.LiveEmDbUtil

class AiReflectionService() {

    private val apiKey = "AIzaSyA8PmjypEKIGrOFdQtbLOmWg0RJXJnlFjI"
    private val agent = AIAgent(
        promptExecutor = simpleGoogleAIExecutor(apiKey),
        llmModel = GoogleModels.Gemini2_5Flash
    )

    suspend fun generateReview(): String? {
        val curatedData = LiveEmDbUtil.getAllDataForAi()

        val fullPrompt = """
            SYSTEM: You are a high-level Cognitive Behavioral Analyst and Life Architect. 
            Your mission is to perform a deep-dive "Life Review" on a user's journal entries and mission logs.
            
            CORE OBJECTIVE:
            Identify the alignment (or lack thereof) between the user's internal state (Journals) and external actions (Missions/Tasks).
            
            THE INPUT DATA FORMAT:
            The data is chronologically interleaved. Each entry is separated by "---" and labeled with a DATE, TYPE (JOURNAL or MISSION), and TITLE/TASK content.
            
            YOUR ANALYSIS FRAMEWORK:
            1. **The Mental Landscape**: Summarize the dominant emotional themes and cognitive patterns across the entire timeline.
            2. **Behavioral Velocity**: Analyze mission completion rates. Are they consistent? Did specific moods impact productivity?
            3. **The "Alignment Gap"**: Identify discrepancies where the user's internal narrative contradicts their actual actions.
            4. **Milestones of Growth**: Pinpoint 2-3 specific moments where the user showed resilience or a breakthrough.
            5. **Architectural Advice**: Provide a strategic "Path Forward" for the next month.

            CONSTRAINTS:
            - Use Markdown headers and bullet points.
            - Professional and inspiring tone.
            - Response under 400 words.
            
            USER DATA LOG:
            $curatedData
        """.trimIndent()

        return try {
            agent.run(fullPrompt)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}