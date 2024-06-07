package apifestivos.apifestivos.presentacion;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import apifestivos.apifestivos.core.interfaces.servicios.IFestivoServicios;

@RestController
@RequestMapping("/api/festivos")
public class FestivoControlador {

    private IFestivoServicios servicio;

    public FestivoControlador(IFestivoServicios servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/verificar/{año}/{mes}/{dia}")
    public String verificar(@PathVariable int año, @PathVariable int mes, @PathVariable int dia) {
        return servicio.verificar(año, mes, dia);
    }

    @GetMapping("/listar/{año}")
    public List<String> listar(@PathVariable int año) {
        return servicio.listar(año);
    }

    @GetMapping("/verificarFecha/{año}/{mes}/{dia}")
    public String verificarFecha(@PathVariable String año, @PathVariable String mes, @PathVariable String dia) {
        try {
            int intAño = Integer.parseInt(año);
            int intMes = Integer.parseInt(mes);
            int intDia = Integer.parseInt(dia);
            LocalDate.of(intAño, intMes, intDia);
            return servicio.verificar(intAño, intMes, intDia);
        } catch (NumberFormatException e) {
            return "Error: Los valores de año, mes y día deben ser números enteros.";
        } catch (DateTimeException e) {
            return "Fecha Inválida";
        }
    }
}