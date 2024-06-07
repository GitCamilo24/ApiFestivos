package apifestivos.apifestivos.aplicacion;

import java.text.SimpleDateFormat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import apifestivos.apifestivos.core.entidades.Festivo;
import apifestivos.apifestivos.core.interfaces.repositorios.IFestivoRepositorio;
import apifestivos.apifestivos.core.interfaces.servicios.IFestivoServicios;

@Service
public class FestivoServicio implements IFestivoServicios {

    private IFestivoRepositorio repositorio;
    private Calendar calendario = Calendar.getInstance();

    
    public FestivoServicio(IFestivoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    // Funciones auxiliares
    private Date agregarDias(Date fecha, int dias) {
        calendario.setTime(fecha);
        calendario.add(Calendar.DATE, dias);
        return calendario.getTime();
    }

    private Date siguienteLunes(Date fecha) {
        calendario.setTime(fecha);
        if (calendario.get(Calendar.DAY_OF_WEEK) > Calendar.MONDAY) {
            fecha = agregarDias(fecha, 9 - calendario.get(Calendar.DAY_OF_WEEK));
        } else {
            fecha = agregarDias(fecha, 1);
        }
        return fecha;
    }

    private int diaDeLaSemana(Date fecha) {
        calendario.setTime(fecha);
        return calendario.get(Calendar.DAY_OF_WEEK);
    }

    private LocalDate obtenerDomingoPascua(int año) {
        int a = año % 19;
        int b = año % 4;
        int c = año % 7;
        int d = (19 * a + 24) % 30;
        int dias = d + (2 * b + 4 * c + 6 * d + 5) % 7;
        int domingoPascua = dias + 22;

        if (domingoPascua > 31) {
            domingoPascua = domingoPascua % 31;
            return LocalDate.of(año, 4, domingoPascua);
        } else {
            return LocalDate.of(año, 3, domingoPascua);
        }
    }

    private Date calcularFechaBasadaEnPascua(int año, int diasPascua) {
        LocalDate domingoPascua = obtenerDomingoPascua(año);
        LocalDate fecha = domingoPascua.plusDays(diasPascua);
        return java.sql.Date.valueOf(fecha);
    }

    private Date festivosFijos(int año, int mes, int dia) {
        calendario.set(año, mes - 1, dia);
        return calendario.getTime();
    }

    @Override
    public List<String> listar(int año) {
        List<Festivo> festivos = repositorio.findAll();
        List<String> festividades = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        for (Festivo festivo : festivos) {
            Date fecha = null;
            switch (festivo.getTipo()) {
                case 1: // Fijo
                    fecha = festivosFijos(año, festivo.getMes(), festivo.getDia());
                    break;
                case 2: // Ley Puente Festivo
                    fecha = festivosFijos(año, festivo.getMes(), festivo.getDia());
                    if (diaDeLaSemana(fecha) != Calendar.MONDAY) { // si la fecha no cae lunes
                        fecha = siguienteLunes(fecha);
                    }
                    break;
                case 3: // Basado en Pascua
                    fecha = calcularFechaBasadaEnPascua(año, festivo.getDiaspascua());
                    break;
                case 4: // Basado en Pascua y Ley Puente Festivo
                    fecha = calcularFechaBasadaEnPascua(año, festivo.getDiaspascua());
                    if (diaDeLaSemana(fecha) != Calendar.MONDAY) { // si la fecha no cae lunes
                        fecha = siguienteLunes(fecha);
                    }
                    break;
                default:
                    break;
            }

            if (fecha != null) {
                festividades.add(sdf.format(fecha) + " - " + festivo.getNombre());
            }
        }
        return festividades;
    }

    @Override
    public String verificar(int año, int mes, int dia) {
        List<Festivo> festivos = repositorio.findAll();
        LocalDate fecha = LocalDate.of(año, mes, dia);

        for (Festivo festivo : festivos) {
            LocalDate festivoFecha = null;
            switch (festivo.getTipo()) {
                case 1:
                case 2:
                    festivoFecha = LocalDate.of(año, festivo.getMes(), festivo.getDia());
                    if (festivo.getTipo() == 2 && festivoFecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                        festivoFecha = festivoFecha.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                    }
                    break;
                case 3:
                case 4:
                    LocalDate domingoPascua = obtenerDomingoPascua(año);
                    festivoFecha = domingoPascua.plusDays(festivo.getDiaspascua());
                    if (festivo.getTipo() == 4 && festivoFecha.getDayOfWeek() != DayOfWeek.MONDAY) {
                        festivoFecha = festivoFecha.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                    }
                    break;
                default:
                    break;
            }

            if (festivoFecha != null && festivoFecha.isEqual(fecha)) {
                return "Es Festivo";
            }
        }
        return "No es festivo";
    }
}