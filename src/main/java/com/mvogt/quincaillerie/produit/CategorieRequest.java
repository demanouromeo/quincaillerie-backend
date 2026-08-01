package com.mvogt.quincaillerie.produit;

import jakarta.validation.constraints.NotBlank;

public record CategorieRequest(@NotBlank String nom) {
}
