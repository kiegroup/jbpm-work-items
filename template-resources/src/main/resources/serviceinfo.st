$widInfo:{k|
$openbracket$
        "handlername" : "$widInfo.(k).name$",
        "handlerclass" : "$widInfo.(k).defaultHandler$",
        $if(widInfo.(k).serviceInfo)$
        "category" : "$widInfo.(k).serviceInfo.category$",
        "keywords" : [
            $widInfo.(k).serviceInfo.keywords:{k1|
            "$k1$"
            }; separator=","$
        ],
        "description" : "$widInfo.(k).serviceInfo.description$",
            $if(widInfo.(k).serviceInfo.hasAction)$
            "isaction" : "true",
            "actiontitle" : "$widInfo.(k).serviceInfo.action.title$"
            $endif$
            $if(widInfo.(k).serviceInfo.hasTrigger)$
            "istrigger" : "true",
            "triggertitle" : "$widInfo.(k).serviceInfo.trigger.title$"
            $endif$
        $endif$
$closebracket$
}; separator=","$
,