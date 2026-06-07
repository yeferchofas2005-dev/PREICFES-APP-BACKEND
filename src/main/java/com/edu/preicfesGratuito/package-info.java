/**
 * Backend de PreICFES Gratuito.
 *
 * <p>Plataforma REST para gestionar simulacros tipo ICFES, usuarios con roles
 * (admin, directivo, profesor, estudiante) y estadísticas de desempeño
 * académico.</p>
 *
 * <h2>Estructura de paquetes</h2>
 * <ul>
 *   <li>{@code config} — configuración de seguridad y beans de aplicación</li>
 *   <li>{@code controller} — endpoints REST expuestos al frontend</li>
 *   <li>{@code dto} — objetos de entrada y salida de la API</li>
 *   <li>{@code entity} — mapeo JPA de tablas MySQL</li>
 *   <li>{@code repository} — acceso a datos y consultas agregadas</li>
 *   <li>{@code service} — lógica de negocio</li>
 *   <li>{@code security} — autenticación JWT y carga de usuarios</li>
 * </ul>
 */
package com.edu.preicfesGratuito;
