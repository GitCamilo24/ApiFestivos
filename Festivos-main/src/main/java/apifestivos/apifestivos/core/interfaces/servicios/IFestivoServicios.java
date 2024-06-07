package apifestivos.apifestivos.core.interfaces.servicios;

import java.util.List;

public interface IFestivoServicios {

    public List<String> listar(int año);
    
    public String verificar(int año, int mes, int dia);
}
