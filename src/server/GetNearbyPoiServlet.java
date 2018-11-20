package server;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.alibaba.fastjson.JSON;
import poi.Poi;
import util.ConstantValues;
import util.Utils;

/**
 * Created by yangjie on 2017/1/8.
 */
public class GetNearbyPoiServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = "";
        double locX = 0;
        double locY = 0;
        int num = 0;
        try {
            keyword = req.getParameter("keyword");
            locX = Double.valueOf(req.getParameter("locX"));
            locY = Double.valueOf(req.getParameter("locY"));
            num = Integer.valueOf(req.getParameter("num"));
        } catch (Exception ex){
            ex.printStackTrace();
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "parameter error");
            return;
        }
        List<Poi> poiArr = getNearbyPoi(keyword,locX,locY, num);
        PrintWriter out = resp.getWriter();
        out.write(JSON.toJSONString(poiArr));
        out.flush();
    }
    private List<Poi> getNearbyPoi(String keyword, double locX, double locY, int num) {
        ArrayList<Poi> poiArr = Utils.loadObjectFromFile(ConstantValues.poiArrObjPath);
        poiArr.sort(new Comparator<Poi>() {
            @Override
            public int compare(Poi o1, Poi o2) {
                double d1 = Utils.calcDistance(o1.locX,o1.locY,locX,locY);
                double d2 = Utils.calcDistance(o2.locX,o2.locY,locX,locY);
                if(d1 - d2 > 0)
                    return 1;
                if(d1 - d2 < 0)
                    return -1;
                return 0;
            }
        });

        return poiArr.subList(0,Math.min(num,poiArr.size()));
    }
}
