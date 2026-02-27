package com.gadgeski.bugmemo.ui.components.deck

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DeepNavy = Color(0xFF0F172A)
val MintCyan = Color(0xFF64FFDA)
val CoolGray = Color(0xFF94A3B8)

@Composable
fun DeckMonitor(logText: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        border = BorderStroke(1.dp, MintCyan.copy(alpha = 0.3f)),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            reverseLayout = false,
        ) {
            item {
                Text(
                    text = logText,
                    color = MintCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
fun DeckConsole(
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onAnalyze: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onReject,
                enabled = !isStreaming,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, if (isStreaming) CoolGray.copy(alpha = 0.5f) else CoolGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CoolGray,
                    disabledContentColor = CoolGray.copy(alpha = 0.5f),
                ),
            ) {
                Text("REJECT")
            }
            Button(
                onClick = onApprove,
                enabled = !isStreaming,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintCyan,
                    contentColor = DeepNavy,
                    disabledContainerColor = MintCyan.copy(alpha = 0.3f),
                    disabledContentColor = DeepNavy.copy(alpha = 0.5f),
                ),
            ) {
                Text("APPROVE", fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onAnalyze,
            enabled = !isStreaming,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = DeepNavy,
                contentColor = MintCyan,
                disabledContainerColor = DeepNavy,
                disabledContentColor = MintCyan.copy(alpha = 0.5f),
            ),
            border = BorderStroke(1.dp, if (isStreaming) MintCyan.copy(alpha = 0.5f) else MintCyan),
        ) {
            Text(if (isStreaming) "EXECUTING..." else "▶ RUN DIAGNOSTICS", fontFamily = FontFamily.Monospace)
        }
    }
}
