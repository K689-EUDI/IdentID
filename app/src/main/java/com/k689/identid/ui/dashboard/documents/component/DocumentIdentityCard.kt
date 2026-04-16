/*
 * Copyright (c) 2025 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European
 * Commission - subsequent versions of the EUPL (the "Licence"); You may not use this work
 * except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the Licence is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific language
 * governing permissions and limitations under the Licence.
 */

package com.k689.identid.ui.dashboard.documents.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.k689.identid.model.core.DocumentIdentifier
import com.k689.identid.ui.component.preview.PreviewTheme
import com.k689.identid.ui.component.preview.ThemeModePreviews
import com.k689.identid.ui.component.utils.SPACING_EXTRA_SMALL
import com.k689.identid.ui.component.utils.SPACING_LARGE
import com.k689.identid.ui.component.utils.SPACING_MEDIUM
import com.k689.identid.ui.component.utils.SPACING_SMALL
import com.k689.identid.ui.component.wrap.WrapCard
import java.util.Locale

val UNIVERSAL_DOCUMENT_CARD_HEIGHT = 212.dp

@Composable
fun DocumentIdentityCard(
    title: String,
    identification: String,
    supportingLines: List<String>,
    status: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    height: Dp = UNIVERSAL_DOCUMENT_CARD_HEIGHT,
) {
    val normalizedSupportingLines = supportingLines.take(2).let { lines -> lines + List((2 - lines.size).coerceAtLeast(0)) { "" } }

    WrapCard(
        modifier = modifier.fillMaxWidth().height(height),
        onClick = onClick,
        throttleClicks = false,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier.padding(SPACING_LARGE.dp),
            verticalArrangement = Arrangement.spacedBy(SPACING_MEDIUM.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = SPACING_SMALL.dp, vertical = SPACING_EXTRA_SMALL.dp),
                        text = identification,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (!status.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = SPACING_SMALL.dp, vertical = SPACING_EXTRA_SMALL.dp),
                            text = status,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(SPACING_EXTRA_SMALL.dp),
            ) {
                normalizedSupportingLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
fun StackedDocumentIdentityCard(
    title: String,
    identification: String,
    supportingLines: List<String>,
    status: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    height: Dp = UNIVERSAL_DOCUMENT_CARD_HEIGHT,
) {
    DocumentIdentityCard(
        title = title,
        identification = identification,
        supportingLines = supportingLines,
        status = status,
        onClick = onClick,
        height = height,
        modifier = modifier,
    )
}

fun DocumentIdentifier.toCardIdentificationTag(): String =
    when (this) {
        DocumentIdentifier.MdocPid -> "mDoc PID"
        DocumentIdentifier.SdJwtPid -> "SD-JWT PID"
        is DocumentIdentifier.OTHER -> {
            val token =
                formatType
                    .substringAfterLast(':')
                    .substringAfterLast('.')
                    .ifBlank { formatType }
            token.replace('_', ' ').uppercase(Locale.getDefault())
        }
    }

@ThemeModePreviews
@Composable
private fun DocumentIdentityCardPreview() {
    PreviewTheme {
        DocumentIdentityCard(
            title = "Personal Identification",
            identification = "Government • mDoc PID",
            supportingLines = listOf("Issuer: National Registry", "Valid until: 15/12/2030"),
            status = "Issued",
            onClick = {},
        )
    }
}
