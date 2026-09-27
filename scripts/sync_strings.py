import re,glob,subprocess,os,sys
ROOT='/home/sklein/IdeaProjects/base-project'
RES=ROOT+'/app/src/main/res'
def parse(text):
    return {m.group(1):m.group(0) for m in re.finditer(r'<string name="(\w+)"[^>]*>.*?</string>',text,re.S)}
def orig(locale):
    return parse(subprocess.check_output(['git','-C',ROOT,'show','HEAD:app/src/main/res/%s/strings.xml'%locale]).decode())
used=set()
for f in glob.glob(ROOT+'/app/src/main/java/**/*.kt',recursive=True):
    used|=set(re.findall(r'R\.string\.(\w+)',open(f).read()))
report=[]
for locale in ['values','values-de']:
    main_path=RES+'/%s/strings.xml'%locale
    main=open(main_path).read()
    main_map=parse(main)
    others={}
    for f in glob.glob(RES+'/%s/*.xml'%locale):
        if f==main_path or f.endswith('themes.xml'): continue
        others.update(parse(open(f).read()))
    o=orig(locale)
    for n in list(main_map):
        if n in others:
            main=main.replace('    '+main_map[n]+'\n','')
            report.append('%s dedupe %s'%(locale,n))
    defined=set(parse(main))|set(others)
    missing=sorted(used-defined)
    add=''
    for n in missing:
        if n in o: add+='    '+o[n]+'\n'; report.append('%s restore %s'%(locale,n))
        else: report.append('%s STILL MISSING %s'%(locale,n))
    if add: main=main.replace('</resources>',add+'</resources>')
    for n in list(parse(main)):
        if n not in used and n not in others:
            main=main.replace('    '+parse(main)[n]+'\n','')
            report.append('%s prune-unused %s'%(locale,n))
    main=re.sub(r'(\n[ \t]*<!--[^>]*?-->[ \t]*)+(?=\n[ \t]*(<!--|</resources>))','',main)
    main=re.sub(r'\n{3,}','\n\n',main)
    open(main_path,'w').write(main)
print('\n'.join(report[:200])); print(len(report),'actions')
