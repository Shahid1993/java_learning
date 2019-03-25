//https://stackoverflow.com/questions/38226742/pass-multiple-values-in-in-clause-using-mybatis-annotations

List<Difference> getDifferencesByScanIDs(@Param("childScanIDs") List<Integer> childScanIDs); // or int[]


SELECT * FROM DBA_COMPARISON_ROW_DIF WHERE SCAN_ID in 
    <foreach item="item" index="index" collection="childScanIDs" open="(" separator="," close=")">
    #{item}
    </foreach>
