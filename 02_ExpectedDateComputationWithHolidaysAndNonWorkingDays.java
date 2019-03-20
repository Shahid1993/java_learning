import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

public class Test {

	public static void main(String[] args) throws ParseException {
		
		System.out.println("Inside Test Class...");
		
		SimpleDateFormat formatter;
		formatter = new SimpleDateFormat("yy-MM-dd");
		
		List<Date> holidayList = new ArrayList<>();
		holidayList.add(formatter.parse("2019-01-01"));
		holidayList.add(formatter.parse("2019-01-15"));
		holidayList.add(formatter.parse("2019-01-26"));
		holidayList.add(formatter.parse("2019-03-22"));
		holidayList.add(formatter.parse("2019-03-23"));		
		holidayList.add(formatter.parse("2019-04-14"));
		holidayList.add(formatter.parse("2019-05-01"));
		holidayList.add(formatter.parse("2019-06-15"));
		holidayList.add(formatter.parse("2019-08-15"));
		holidayList.add(formatter.parse("2019-10-02"));
		holidayList.add(formatter.parse("2019-11-06"));
		holidayList.add(formatter.parse("2019-12-25"));		
		
		Date expectedDeliveryDate = new Test().incrementDaysExcludingWeekends(new Date(), 6, holidayList);
		
		System.out.println(expectedDeliveryDate);
	}
	
	
	public Date incrementDaysExcludingWeekends(Date date, int increment, List<Date> holidayList) throws ParseException {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        
        for(int i=0; i< increment; i++) {
        	c.add(Calendar.DAY_OF_WEEK, 1);
        	while(!isWorkingDay(c) || isHoliday(c, holidayList)) {
                c.add(Calendar.DAY_OF_WEEK, 1);
            }
        }
        
        return c.getTime();
          
    }

    private boolean isWorkingDay(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        
        /**check if it is Saturday(day=7) or Sunday(day=1) */
        if ((dayOfWeek == 7) || (dayOfWeek == 1)) {
            return false;
        }
        return true;
     }
    
    private boolean isHoliday(Calendar calendar1, List<Date> holidayList) {
    	
    	if(CollectionUtils.emptyIfNull(holidayList).isEmpty())
    		return false;
    	
    	for(int i=0; i<holidayList.size(); i++) {
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(holidayList.get(i));
            
    		if(calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
    				&& calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH))
    			return true;
    	}
    	
    	return false;
     }
}
