var fs = require('fs');
var inputJSON = JSON.parse(fs.readFileSync('./config/UnixMetrics.json', 'utf8'));

inputJSON.forEach(function(os) {
    var outfile = fs.createWriteStream('./config/' + os.os + '.new.json');
    var outJSON = {
        'os' : os.os,
        'pageSize': os.pageSize,  
    };
    
    if("pageSizeCommand" in os) {
        outJSON.pageSizeCommand = os.pageSizeCommand.join(" ");
    }
    outJSON['commands'] = {};    

    var metrics = {};
    for(var metric in os.allMetrics) {
        var splitname = metric.split("/");
        if(!(splitname[0] in metrics)) {
            metrics[splitname[0]] = {};
        }
        var metricout = os.allMetrics[metric];
        metricout.type = metricout.this_type;
        delete metricout.this_type;
        metrics[splitname[0]][splitname[1]] = metricout;
    }

    for(var comm in os.allCommands) {
        var oldcomm = os.allCommands[comm];
        var newcomm = {
            "command" : oldcomm.commands[0].join(" "),
            "checkAllRegex" : oldcomm.checkAllRegex,
            "lineLimit" : oldcomm.lineLimit,
            "type" : oldcomm.type,
            "mappings": oldcomm.lineMappings,
            "metrics" : metrics[comm]   
        };

        outJSON.commands[comm] = newcomm;
    } 

    outfile.write(JSON.stringify(outJSON, null, 2));
    outfile.close;
});