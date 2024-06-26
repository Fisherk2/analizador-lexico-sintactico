package lexico;

import lenguaje.Automata;

import java.util.LinkedList;
import java.util.regex.PatternSyntaxException;

/**
 * Clase que almacena las propiedades de un analizador lexico.
 */
public class Lexic {

    //▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼ VARIABLES ▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼//
    private final Automata AFN;
    private final LinkedList<String> PRIORIDAD_CLASIFICACION;
    private final Clasificacion[] TABLA_CLASIFICACION_LEXICA;
    private final LinkedList<Clasificacion> TABLA_PALABRAS_RESERVADAS;
    private final LinkedList<Token> TABLA_SIMBOLOS;

    private int numIdentificadores;

    /**
     * Clase que almacena las propiedades de un analizador lexico dependiendo del automata y tabla de clasificacion lexica.
     *
     * @param automata                   Automata finito no determinista.
     * @param tabla_clasificacion_lexica Tabla de clasificacion lexica que contenga descripcion de clasificacion, regex y atributo.
     * @implNote La tabla de clasificacion lexica DEBE ESTAR ORDENADO DEPENDIENDO DE LA PRIORIDAD DE CLASIFICACION LEXICA,
     * por lo tanto, las PRIMERAS clasificaciones deben ser palabras reservadas, en SEGUNDO los caracteres simples, y
     * AL FINAL de la tabla, siempre debe ir los identificadores.
     */
    public Lexic(Automata automata, Clasificacion[] tabla_clasificacion_lexica) {

        TABLA_PALABRAS_RESERVADAS = new LinkedList<>();
        TABLA_SIMBOLOS = new LinkedList<>();
        PRIORIDAD_CLASIFICACION = new LinkedList<>();
        AFN = automata;
        TABLA_CLASIFICACION_LEXICA = tabla_clasificacion_lexica;

        numIdentificadores = -1;

        // ◂ ◂ ◂ ◂ Almacenamos clasificaciones en orden de prioridad sin duplicados ▸ ▸ ▸ ▸ //
        for (Clasificacion clasificacion : TABLA_CLASIFICACION_LEXICA) {

            // ◂ ◂ ◂ ◂ Evitamos duplicados y lo almacenamos ▸ ▸ ▸ ▸ //
            if (!PRIORIDAD_CLASIFICACION.contains(clasificacion.clasificacion)) {
                PRIORIDAD_CLASIFICACION.add(clasificacion.clasificacion);
            }
        }

        // ◂ ◂ ◂ ◂ Almacenamos las palabras reservadas ▸ ▸ ▸ ▸ //
        for (Clasificacion clasificacion : TABLA_CLASIFICACION_LEXICA) {

            // ◂ ◂ ◂ ◂ La PRIMER PRIORIDAD de la tabla de clasificacion lexica siempre seran palabras reservadas ▸ ▸ ▸ ▸ //
            if (clasificacion.clasificacion.equals(getPrioridadLexica(0))) {
                TABLA_PALABRAS_RESERVADAS.add(clasificacion);
            }
        }
    }

    @Override
    public String toString() {

        String resultado = "\n▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ LEXICO ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼ △ ▼\n";

        resultado += "\n⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖  TABLA DE PALABRAS RESERVADAS ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖\n";

        for (Clasificacion clasificacion : TABLA_PALABRAS_RESERVADAS) {
            resultado += clasificacion + "\n";
        }

        resultado += "\n⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖  TABLA DE SIMBOLOS COMPLETA ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖ ⧗ ⧖\n";

        for (Token token : TABLA_SIMBOLOS) {
            resultado += token + "\n";
        }

        return resultado;
    }

    //▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼ GETTERS & SETTERS ▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼▲▼//

    /**
     * Funcion que sirve para saber que tipo de clasificacion lexica pertenece al lexema procesado.
     *
     * @param lexema Nombre propio, cadena, palabra que NO ESTE VACIA.
     * @return Clasificacion lexica que pertenece a dicho lexema.
     */
    public Clasificacion queClasificacionEs(String lexema) {
        for (Clasificacion clasificacion : TABLA_CLASIFICACION_LEXICA) {
            try {

                // ◂ ◂ Si alguna de las expresiones regulares que se encuentren en las clasificaciones coincide ▸ ▸ //
                if (lexema.matches(clasificacion.regex)) {
                    return clasificacion;
                }

            } catch (PatternSyntaxException ex) {
                System.err.println("ERROR, NO PUEDE ANALIZAR LA EXPRESION REGULAR: " + clasificacion.regex + "\n CORRIJALO PARA CONTINUAR EVALUANDO.");
            }

        }

        return new Clasificacion("LEXER ERROR", "", -1);
    }

    /**
     * Funcion que genera un token de los lexemas que son aceptados o rechazados por el AFN.
     *
     * @param lexema     Nombre propio, cadena, palabra.
     * @param numDeLinea Numero de linea de codigo donde se ubica el lexema.
     */
    public Token crearToken(String lexema, int numDeLinea) {

        // ◂ ◂ ◂ ◂ Si el lexema no es aceptado, se generara un token de error donde se almacena el numero de linea ▸ ▸ ▸ ▸ //
        if (!AFN.esAceptada(lexema)) {
            return new TokenError(lexema, numDeLinea);
        }

        // ◂ ◂ ◂ ◂ Asignar numero de atributo en cada identificador diferente comenzando desde su atributo base ▸ ▸ ▸ ▸ //
        if (queClasificacionEs(lexema).clasificacion.equals(getPrioridadLexica(-1))) {
            numIdentificadores++;
            return new Token(lexema, queClasificacionEs(lexema).atributo + numIdentificadores);
        }

        return new Token(lexema, queClasificacionEs(lexema).atributo);
    }

    /**
     * Funcion que devuelve el titulo de clasificacion lexica dependiendo de su prioridad de analisis lexico.
     *
     * @param nivel_de_prioridad Nivel de prioridad donde el 0 es el mas importante.
     * @return Titulo de la clasificacion lexica perteneciente a ese nivel de prioridad.
     * @implNote 0 = Palabras reservadas,
     * 1 = Caracteres Simples,. . .
     * n-1 ó n < 0 = Identificadores.
     */
    public String getPrioridadLexica(int nivel_de_prioridad) {

        if (nivel_de_prioridad >= PRIORIDAD_CLASIFICACION.size() || nivel_de_prioridad < 0) {
            return PRIORIDAD_CLASIFICACION.getLast();
        }

        return PRIORIDAD_CLASIFICACION.get(nivel_de_prioridad);
    }

    /**
     * Metodo que almacena Tokens en la tabla de simbolos.
     *
     * @param token Token con atributo y lexema.
     */
    public void almacenarSimbolo(Token token) {

        // ◂ ◂ ◂ ◂ ¿Es identificador? se almacena sin repetir en caso que lo sea ▸ ▸ ▸ ▸ //
        if (queClasificacionEs(token.LEXEMA).clasificacion.equals(getPrioridadLexica(-1))) {
            if (TABLA_SIMBOLOS.isEmpty() || !TABLA_SIMBOLOS.contains(token)) {
                TABLA_SIMBOLOS.add(token);
            }
        }
    }

    /**
     * Metodo que devuelve la tabla de simbolos del analizador lexico.
     *
     * @return Tabla de simbolos actual.
     */
    public LinkedList<Token> getTABLA_SIMBOLOS() {
        return TABLA_SIMBOLOS;
    }
}

