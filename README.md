# El Llorón — mod de terror para Minecraft Forge 1.20.1

## Qué hace
**El Llorón** es un PNJ de terror con tres fases:

1. **Acecho (stalker):** te sigue de lejos y se te va acercando de a poco. Si lo mirás se congela. Si lo mirás mucho tiempo desaparece y reaparece a tu espalda. Si lo mirás de cerca, se enoja.
2. **Caza:** pega un grito y te corre un poquito más rápido que tu sprint. No podés escapar corriendo: tenés que esconderte.
3. **Búsqueda:** si te metés a un armario sin que te vea, te busca y se rinde. **Si te vio entrar, golpea el armario y a los 6 segundos te saca a la fuerza (8 de daño).**

## Esconderte
- Fabricá el **Armario para esconderse** (8 tablones de roble oscuro + 1 lingote de hierro en el centro) o sacalo del creativo.
- Click derecho para entrar (quedás invisible e inmóvil). **Shift** para salir.

## Sonidos (sintetizados, todos propios)
Llanto, susurros, grito de caza, pasos, crujido/golpes del armario, latidos del corazón durante la caza y un zumbido grave de tensión mientras te acecha.

## Probarlo
- Huevo de aparición: pestaña "Huevos de aparición" del creativo.
- También aparece solo, de noche o en lugares oscuros del Overworld (no en Pacífico).

## Compilar
**Opción fácil (sin instalar nada):** subí esta carpeta a un repo de GitHub. En la pestaña *Actions* corre solo "Compilar mod"; al terminar bajás el `.jar` de *Artifacts* y lo ponés en `mods/`.

**En tu PC:** JDK 17 + Gradle 8.1.1 → `gradle build`. El jar queda en `build/libs/`.

Requiere Minecraft **1.20.1** con **Forge 47.x**.

## Ajustes rápidos (WeeperEntity.java)
- Velocidad de caza: `getNavigation().moveTo(target, 1.15D)`
- Tiempo para que te saque del armario: `knockTicks >= 120`
- Frecuencia de aparición: `HorrorDirector.java` (probabilidad 0.3 cada 30 s)
