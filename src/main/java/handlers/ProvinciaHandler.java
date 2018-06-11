package handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import configs.Database;
import models.Provincia;

public class ProvinciaHandler{
  public static Route listar = (Request request, Response response) -> {
    String rpta = "";
    int departamentoId = Integer.parseInt(request.params(":departamento_id"));
    Database db = new Database();
    try {
      List<JSONObject> rptaTemp = new ArrayList<JSONObject>();
      db.open();
      List<Provincia> rptaList = Provincia.find("departamento_id = ?", departamentoId);
      for (Provincia Provincia : rptaList) {
        JSONObject obj = new JSONObject();
        obj.put("id", Provincia.get("id"));
        obj.put("nombre", Provincia.get("nombre"));
        rptaTemp.add(obj);
      }
      rpta = rptaTemp.toString();
    }catch (Exception e) {
      String[] error = {"Se ha producido un error en  listar las provincias del departamento", e.toString()};
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
      int departamentoId = data.getJSONObject("extra").getInt("departamento_id");
      db.open();
      db.getDb().openTransaction();
      if(nuevos.length() > 0){
        for (int i = 0; i < nuevos.length(); i++) {
          JSONObject provincia = nuevos.getJSONObject(i);
          String antiguoId = provincia.getString("id");
          String nombre = provincia.getString("nombre");
          Provincia n = new Provincia();
          n.set("nombre", nombre);
          n.set("departamento_id", departamentoId);
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
          JSONObject provincia = editados.getJSONObject(i);
          int id = provincia.getInt("id");
          String nombre = provincia.getString("nombre");
          Provincia e = Provincia.findFirst("id = ?", id);
          if(e != null){
            e.set("nombre", nombre);
            e.saveIt();
          }
        }
      }
      if(eliminados.length() > 0){
        for (Object eliminado : eliminados) {
          int eleminadoId = (Integer)eliminado;
          Provincia d = Provincia.findFirst("id = ?", eleminadoId);
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
      String[] cuerpoMensaje = {"Se ha producido un error en  guardar las provincias", execption};
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "error");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      response.status(500);
      rpta = rptaMensaje.toString();
    }else{
      JSONArray cuerpoMensaje =  new JSONArray();
      cuerpoMensaje.put("Se ha registrado los cambios en las provincias");
      cuerpoMensaje.put(listJSONNuevos);
      JSONObject rptaMensaje = new JSONObject();
      rptaMensaje.put("tipo_mensaje", "success");
      rptaMensaje.put("mensaje", cuerpoMensaje);
      rpta = rptaMensaje.toString();
    }
    return rpta;
  };
}