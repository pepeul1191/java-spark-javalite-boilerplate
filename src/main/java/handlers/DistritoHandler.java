package handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import configs.Database;
import models.Distrito;
import models.VWDistritoProvinciaDepartamento;

public class DistritoHandler{
  public static Route listar = (Request request, Response response) -> {
    String rpta = "";
    int provinciaId = Integer.parseInt(request.params(":provincia_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      List<Distrito> rptaList = Distrito.find("provincia_id = ?", provinciaId);
      for (Distrito Distrito : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", Distrito.get("id"));
        obj.put("nombre", Distrito.get("nombre"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar los distritos de la provincia", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", error);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      db.close();
    }
    return rpta;
  };

  public static Route buscar = (Request request, Response response) -> {
    String rpta = "";
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      String query = "nombre LIKE '" + request.queryParams("nombre")+ "%'";
      List<VWDistritoProvinciaDepartamento> rptaList = VWDistritoProvinciaDepartamento.where(query).limit(10);
      for (VWDistritoProvinciaDepartamento distrito : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", distrito.get("id"));
        obj.put("nombre", distrito.get("nombre"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar la búsqueda", e.toString()};
      JSONObject rptaTry = new JSONObject();
      rptaTry.put("tipo_mensaje", "error");
      rptaTry.put("mensaje", error);
      rpta = rptaTry.toString();
      response.status(500);
    } finally {
      db.close();
    }
    return rpta;
  };

  public static Route guardar = (Request request, Response response) -> {
    String rpta = "";
    List<JSONObject> listJSONNuevos = new ArrayList<JSONObject>();
    boolean error = false;
    String execption = "";
    Database db = new Database();
    try {
      JSONObject data = new JSONObject(request.queryParams("data"));
      JSONArray nuevos = data.getJSONArray("nuevos");
      JSONArray editados = data.getJSONArray("editados");
      JSONArray eliminados = data.getJSONArray("eliminados");
      int provinciaId = data.getJSONObject("extra").getInt("provincia_id");
      db.open();
      db.getDb().openTransaction();
      if(nuevos.length() > 0){
        for (int i = 0; i < nuevos.length(); i++) {
          JSONObject distrito = nuevos.getJSONObject(i);
          String antiguoId = distrito.getString("id");
          String nombre = distrito.getString("nombre");
          Distrito n = new Distrito();
          n.set("nombre", nombre);
          n.set("provincia_id", provinciaId);
          n.saveIt();
          int nuevoId = (int) n.get("id"); 
          JSONObject temp = new JSONObject();
          temp.put("temporal", antiguoId);
          temp.put("nuevo_id", nuevoId);
          listJSONNuevos.add(temp);
        }
      }
      if(editados.length() > 0){
        for (int i = 0; i < editados.length(); i++) {
          JSONObject distrito = editados.getJSONObject(i);
          int id = distrito.getInt("id");
          String nombre = distrito.getString("nombre");
          Distrito e = Distrito.findFirst("id = ?", id);
          if(e != null){
            e.set("nombre", nombre);
            e.saveIt();
          }
        }
      }
      if(eliminados.length() > 0){
        for (Object eliminado : eliminados) {
          int eleminadoId = (Integer)eliminado;
          Distrito d = Distrito.findFirst("id = ?", eleminadoId);
          if(d != null){
            d.delete();
          }
        }
      }
      db.getDb().commitTransaction();
    }catch (Exception e) {
      error = true;
      //e.printStackTrace();
      execption = e.toString();
    } finally {
      if(db.getDb().hasConnection()){
        db.close();
      }
    }
    if(error){
      String[] cuerpoMensaje = {"Se ha producido un error en  guardar los distritos", execption};
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "error");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      response.status(500);
      rpta = rptaMensaje.toString();
    }else{
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado los cambios en los distritos");
      cuerpoMensaje.put(listJSONNuevos);
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }
    return rpta;
  };
}